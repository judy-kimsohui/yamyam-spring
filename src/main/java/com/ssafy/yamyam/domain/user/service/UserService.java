package com.ssafy.yamyam.domain.user.service;


import com.ssafy.yamyam.domain.user.dto.SignupRequestDto;
import com.ssafy.yamyam.domain.user.dto.UserDto;
import com.ssafy.yamyam.domain.user.dto.UserUpdateRequestDto;
import com.ssafy.yamyam.domain.user.mapper.UserMapper;
import com.ssafy.yamyam.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {


    private final UserMapper userMapper;

    @Value("${spring.servlet.multipart.location}")
    private String uploadDir;

    @Transactional
    public void signup(SignupRequestDto signupDto){
        User existingUser = userMapper.findByUserId(signupDto.getUserId());
        if(existingUser != null){
            //이미 존재하는 유저 아이디
            throw new IllegalArgumentException("이미 존재하는 아이디");
        }

        User user = new User();
        user.setUserId(signupDto.getUserId());
        user.setPassword(signupDto.getPassword()); // 필요 시 암호화 적용 공간
        user.setNickName(signupDto.getNickName());
        user.setAge(signupDto.getAge());
        user.setHeight(signupDto.getHeight());
        user.setWeight(signupDto.getWeight());
        user.setGoalWeight(signupDto.getGoalWeight());

        try {
            if (signupDto.getGender() != null) {
                user.setGender(User.Gender.valueOf(signupDto.getGender().toUpperCase()));
            } else {
                user.setGender(User.Gender.NONE);
            }
        } catch (IllegalArgumentException e) {
            user.setGender(User.Gender.NONE); // 매칭 안 되는 이상한 문자열이 들어오면 NONE 처리
        }

        // 4. 프로필 이미지 파일 처리 (업로드 파일이 존재할 때만)
        MultipartFile file = signupDto.getProfileImgFile();
        if (file != null && !file.isEmpty()) {
            String savedPath = saveProfileImage(file);
            user.setProfileImg(savedPath); // 서버에 저장된 상대/절대 파일 경로 기록
        } else {
            user.setProfileImg("default_profile.png"); // 이미지가 없을 때의 기본 이미지 경로
        }

        // 5. DB 마스터 테이블 저장
        userMapper.insertUser(user);
    }
    private String saveProfileImage(MultipartFile file) {
        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs(); // 디렉토리가 없으면 생성
        }

        // 파일명 중복을 피하기 위해 UUID 사용
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = UUID.randomUUID().toString() + extension;

        try {
            file.transferTo(new File(folder, savedFilename));
            return "/uploads/" + savedFilename; // 웹 브라우저가 나중에 접근할 수 있는 가상 URI 경로 리턴
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    public User login(String userId, String password){

        User user = userMapper.findByUserId(userId);

        if(user == null) throw new IllegalArgumentException("존재하지 않는 아이디");
        if(!user.getPassword().equals(password)) throw new IllegalArgumentException("비밀번호가 틀렸습니다.");

        return user;
    }

    public User getUserInfo(Long id){
        return userMapper.findById(id);
    }

    @Transactional
    public void updateProfile(Long id, UserUpdateRequestDto updateDto){
        User user = userMapper.findById(id);
        if(user == null) throw new IllegalArgumentException("해당 유저를 찾을 수 없습니다.");


        user.setNickName(updateDto.getNickName());
        user.setAge(updateDto.getAge());
        user.setUserGoal(updateDto.getUserGoal());
        // 성별
        try {
            user.setGender(updateDto.getGender() != null ? User.Gender.valueOf(updateDto.getGender().toUpperCase()) : User.Gender.NONE);
        } catch (IllegalArgumentException e) {
            user.setGender(User.Gender.NONE);
        }
        user.setHeight(updateDto.getHeight());
        user.setWeight(updateDto.getWeight());
        user.setGoalWeight(updateDto.getGoalWeight());

        int updated = userMapper.updateUserProfile(user);
        if (updated == 0) {
            throw new IllegalStateException("프로필 업데이트에 실패했습니다. (id=" + id + ")");
        }
    }

    public UserDto findUserById(Long loginUserKey) {
        User user = userMapper.findById(loginUserKey);
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUser_id(user.getUserId());
        dto.setNick_name(user.getNickName());
        dto.setProfile_img(user.getProfileImg());
        dto.setAge(user.getAge());
        dto.setGender(user.getGender() != null ? user.getGender().name() : null);
        dto.setHeight(user.getHeight());
        dto.setWeight(user.getWeight());
        dto.setGoal_weight(user.getGoalWeight());
        dto.setUser_goal(user.getUserGoal() != null ? user.getUserGoal() : "");

        return dto;
    }
}
