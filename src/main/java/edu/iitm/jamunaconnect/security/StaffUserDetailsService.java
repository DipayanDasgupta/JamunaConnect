package edu.iitm.jamunaconnect.security;

import edu.iitm.jamunaconnect.domain.StaffUser;
import edu.iitm.jamunaconnect.repository.StaffUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffUserRepository staffUsers;

    public StaffUserDetailsService(StaffUserRepository staffUsers) {
        this.staffUsers = staffUsers;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StaffUser user = staffUsers.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No staff user: " + username));
        return new User(
                user.getUsername(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}