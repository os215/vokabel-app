package net.suevern.vokabel.security;

import net.suevern.vokabel.entity.User;
import net.suevern.vokabel.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Internal repository users can only be VISITOR
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .filter(role -> "VISITOR".equals(role)) // Only allow VISITOR role
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());

        // Ensure at least VISITOR role is present
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_VISITOR"));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .build();
    }

    /**
     * Creates a new internal user. Internal users can only have VISITOR role.
     * Any other roles will be ignored and replaced with VISITOR.
     */
    public User createUser(String email, String encodedPassword, String name) {
        User user = new User(email, encodedPassword, name);
        // Internal users are always VISITOR only
        user.setRoles(Set.of("VISITOR"));
        return userRepository.save(user);
    }

    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }
}

