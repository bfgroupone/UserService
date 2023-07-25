package groupone.userservice.config;


import groupone.userservice.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//WebSecurityConfigurerAdapter needs to extended to override some of its methods
@Configuration
@EnableWebSecurity
@PropertySource("classpath:application.properties")
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private UserDetailsService userDetailsService;

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


    // authentication provider uses the userDetailsService by calling the loadUserByUsername()
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder());
        return provider;
    }

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    private JwtFilter jwtFilter;

    @Autowired
    public void setJwtFilter(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


    //This method is used to configure the security of the application
    //Since we are attaching jwt to a request header manually, we don't need to worry about csrf

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().and()
                .logout().clearAuthentication(true)
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .and()

                .authorizeRequests()
                .antMatchers("/user-service/login").permitAll()
                .antMatchers("/user-service/register").permitAll()
                .antMatchers(HttpMethod.GET, "/user-service/validate").permitAll()
                .antMatchers(HttpMethod.POST, "/user-service/validate").permitAll()
                .antMatchers(HttpMethod.GET,"/user-service/users").hasAuthority("admin_read")
                .antMatchers(HttpMethod.PATCH, "/user-service/users/*/active").hasAnyAuthority("ban_unban")
                .antMatchers(HttpMethod.GET, "/user-service/users").hasAnyAuthority("admin_read")
                .antMatchers(HttpMethod.PATCH, "/user-service/users/*").hasAnyAuthority("update")
                .antMatchers(HttpMethod.PATCH, "/user-service/users/*/active").hasAnyAuthority("ban_unban")
                .antMatchers(HttpMethod.PATCH, "/user-service/users/*/promote").hasAnyAuthority("promote")
                .antMatchers(HttpMethod.GET, "/user-service/user").hasAnyAuthority("read")
                .antMatchers(HttpMethod.DELETE, "/user-service/user").hasAnyAuthority("admin_read")
                .anyRequest()
                .authenticated().and().addFilterAfter(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
