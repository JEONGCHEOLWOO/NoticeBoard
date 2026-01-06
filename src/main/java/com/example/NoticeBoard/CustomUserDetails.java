package com.example.NoticeBoard;

import com.example.NoticeBoard.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// 인증 방식(소셜 로그인 or 로컬 로그인)과 무관하게 통합하여 하나의 User 엔터티로 User에 정보를 가져오기 위한 클래스
// 로그인한 사용자를 판별하는 최소 정보만 가지고 있는 클래스
// SecurityContext에서 User 정보를 안정적으로 꺼내기 위함
// SecurityContext는 세션/ 요청마다 유지가 되기 때문에 데이터가 변경되는 필드는 사용x (ex. nickname, profileImage, status 등)
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user){
        this.user = user;
    }

    // 엔티티 식별 (댓글 작성자 비교)
    public Long getId(){
        return user.getId();
    }

    // 권한 판단
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Spring Security 식별자
    // username이 아니라 email을 리턴하는 이유는 username(사용자 이름)은 동명이인이 있을 수 있어서 식별할 수 없기 때문에
    // 식별이 가능한 User 엔터티에서 PK값을 가지고 있는 email로 리턴함.
    @Override
    public String getUsername() {
        return user.getEmail();
    }

}
