package com.example.jwt.Service;

import com.example.jwt.Config.JwtTokenUtil;
import com.example.jwt.Model.*;
import com.example.jwt.Repository.*;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Service
public class ProductsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder bcryptEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private ProductsRepository productsRepository;

    public ProductsDao save(String token, ProductDto productDto) {
        ProductsDao newProduct = new ProductsDao();
        StatusDao statusDao = statusRepository.findByName("ACTIVE_PRODUCT");
        newProduct.setName(productDto.getName());
        newProduct.setPrice(productDto.getPrice());
        newProduct.setAmount(productDto.getAmount());
        newProduct.setTagId(tagRepository.findById(productDto.getTagId()));
        newProduct.setOwnerId(getUserId(token));
        newProduct.setStatusDao(statusDao);

        return productsRepository.save(newProduct);
    }

    public List getProductsList(String token) {
        String name = userRepository.findById(getUserId(token)).getUsername();
        List<ProductList<Long, String, TagDao, StatusDao, Integer, Long, Integer, Timestamp, Timestamp>> listProduct = new ArrayList<>();
        if (name.equals("Admin")) {
            Iterable<ProductsDao> iterable = productsRepository.findAll();

            iterable.forEach(s -> {
                listProduct.add(new ProductList(s.getId(), s.getName(), s.getTagId(), s.getStatusDao(), s.getPrice(), s.getOwnerId(), s.getAmount(), s.getCreatedAt(), s.getUpdatedAt()));
            });
            return listProduct;
        }else {
            StatusDao statusDao = statusRepository.findByName("ACTIVE_PRODUCT");
            Iterable<ProductsDao> iterable = productsRepository.findAllByStatusDao(statusDao);

            iterable.forEach(s -> {
                listProduct.add(new ProductList(s.getId(), s.getName(), s.getTagId(), s.getStatusDao(), s.getPrice(), 0, s.getAmount(), s.getCreatedAt(), s.getUpdatedAt()));
            });
            return listProduct;
        }
    }

    public ProductsDao update(String token, ProductDto productDto) {
        ProductsDao updateProduct = productsRepository.findById(productDto.getId());
        String name = userRepository.findById(getUserId(token)).getUsername();

        if (updateProduct.getOwnerId() == getUserId(token) || name.equals("Admin")) {
            if (productDto.getName() != null) {
                updateProduct.setName(productDto.getName());
            }

            if (productDto.getPrice() > 0) {
                updateProduct.setPrice(productDto.getPrice());
            }

            if (productDto.getAmount() > 0) {
                updateProduct.setAmount(productDto.getAmount());
            }

            if (productDto.getTagId() > 0) {
                updateProduct.setTagId(tagRepository.findById(productDto.getTagId()));
            }


            return productsRepository.save(updateProduct);
        } else {
            return null;
        }
    }

    public ProductsDao delete(String token,ProductDto productDto) {
        ProductsDao deleteProduct = productsRepository.findById(productDto.getId());
        String name = userRepository.findById(getUserId(token)).getUsername();

        if (deleteProduct.getOwnerId() == getUserId(token) || name.equals("Admin")) {
            StatusDao statusDao = statusRepository.findByName("DELETED");
            deleteProduct.setStatusDao(statusDao);
            productsRepository.save(deleteProduct);
            return deleteProduct;
        }else {
            return null;
        }

    }

    private long getUserId(String token) {
        String username = null;
        String jwtToken = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (token != null && token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            }
        }
        UserDao userDao = new UserDao();
        //Once we get the token validate it.
        if (username != null) {

            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
            System.out.println(username);
            userDao = userRepository.findByUsername(userDetails.getUsername());
            System.out.println(userDetails.getUsername());
            System.out.println(userDao.getId());
        }
        return userDao.getId();
    }


}
