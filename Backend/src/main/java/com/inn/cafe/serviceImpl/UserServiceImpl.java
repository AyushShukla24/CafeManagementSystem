package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.CustomerUserDetailService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.JwtUtils;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstant;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtils;
import com.inn.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    CustomerUserDetailService customerUserDetailService;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    JwtFilter jwtFilter;
    @Autowired
    EmailUtils emailUtils;
    @Override
    public ResponseEntity<String> signup(Map<String, String> requestMap) {
        try{
            log.info("inside singup {}", requestMap);
            if(validateSignUp(requestMap)){
                User user = userDao.findByEmailId(requestMap.get("email"));
                log.info("user find or not", user);

                if(Objects.isNull(user)){
                    User newuUser = getUserObject(requestMap);
                    userDao.save(newuUser);
                    return CafeUtils.getResponseEntity("Successfully Registered",HttpStatus.OK);
                }
                else{
                   return CafeUtils.getResponseEntity("Email Already Exists",HttpStatus.BAD_REQUEST);
                }
            }
            else{
                return CafeUtils.getResponseEntity(CafeConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception ex){
            ex.getStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public boolean validateSignUp(Map<String,String> requestMap){
        if(requestMap.containsKey("name") && requestMap.containsKey("email") && requestMap.containsKey("password")
        && requestMap.containsKey("contactNumber")){
            return true;
        }
        else
            return false;
    }

    public User getUserObject(Map<String,String> requestMap){
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get(("email")));
        user.setPassword(requestMap.get("password"));
        user.setRole("user");
        user.setStatus(("false"));

        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap){
        log.info("Inside login");
        try{
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"),requestMap.get("password"))
                    );

                    if(customerUserDetailService.getUserDetails().getStatus().equalsIgnoreCase("true")){
                        return new ResponseEntity<String>(
                                "{\"token\":\"" + jwtUtils.generateToken(customerUserDetailService.getUserDetails().getEmail(), customerUserDetailService.getUserDetails().getRole()) + "\"}",
                                HttpStatus.OK
                        );
                    }
            return new ResponseEntity<String>("{\"message\": \"Wait for admin approval.\"}", HttpStatus.BAD_REQUEST);

        }
        catch (Exception ex){
        log.info("{}"+ ex);
        }
        return new ResponseEntity<String>("{\"message\": \"Bad Credentials\"}", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser(){
        log.info("Inside getAllUser");
        try {
            log.info("is admin ? "+jwtFilter.isAdmin());
            if(jwtFilter.isAdmin()){
                log.info("all users",userDao.getAllUser());
                return new ResponseEntity(userDao.getAllUser(),HttpStatus.OK);
            }
            else{
                return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));

                if(!optional.isEmpty()){
                    userDao.update(requestMap.get("status"),Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmin(requestMap.get("status"),optional.get().getEmail(),userDao.getAllAdmin());
                    return CafeUtils.getResponseEntity("User status updated successfully",HttpStatus.OK);
                }
                else{
                    return CafeUtils.getResponseEntity("User Id does'nt exist",HttpStatus.OK);
                }
            }
        }
        catch (Exception ex){
            ex.getStackTrace();
        }
        return new ResponseEntity<String>(CafeConstant.UNAUTHORIZED_ACCESS, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getUserName());
        if(status != null && status.equalsIgnoreCase("true")){
            emailUtils.sendSimpleMessage(jwtFilter.getUserName(),"Account Approved","USER:- "+user+" \n is approved bt \n ADMIN:-"+jwtFilter.getUserName(), allAdmin);
        } else {
            emailUtils.sendSimpleMessage(jwtFilter.getUserName(),"Account Disabled","USER:- "+user+" \n is disabled bt \n ADMIN:-"+jwtFilter.getUserName(), allAdmin);
        }
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return CafeUtils.getResponseEntity("true",HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String,String> requestMap) {
       try{
            User user = userDao.findByEmail(jwtFilter.getUserName());
            if(!user.equals(null)){
                if(user.getPassword().equals(requestMap.get("oldPassword"))){
                    user.setPassword(requestMap.get("newPassword"));
                    userDao.save(user);
                    return CafeUtils.getResponseEntity("Password Updated Successfully",HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Password is Incorrect",HttpStatus.BAD_REQUEST);
            }

            return CafeUtils.getResponseEntity("No User Found",HttpStatus.BAD_REQUEST);
       }
       catch (Exception ex){

       }
        return new ResponseEntity<String>(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgetPassword(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmail(requestMap.get("email"));

            if(!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())){
                emailUtils.forgotMail(user.getEmail(),"Credentials from Cafe Management", user.getPassword());
                return CafeUtils.getResponseEntity("Sent Credentials on User's Mail", HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("No User Found",HttpStatus.BAD_REQUEST);
        }
        catch (Exception ex){
            ex.getStackTrace();
        }
        return new ResponseEntity<String>(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
