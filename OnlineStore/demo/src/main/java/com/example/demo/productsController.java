package com.example.demo;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class productsController {
    @Autowired
    private productrepository repo;

    @GetMapping({"" , "/"})
    public String showProductsList(Model model){
        List<product> products = repo.findAll(Sort.by("id"));
        model.addAttribute("products",products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        productdto productDto = new productdto();
        model.addAttribute("productDto",productDto);
        return "products/CreateProduct";
    }
    
    @PostMapping("/create")
    public String createProduct(@Valid  @ModelAttribute productdto productDto,BindingResult result){
if(productDto.getImagefile().isEmpty()){
    result.addError(new FieldError("productDto","imagefile","the image"));
}
if(result.hasErrors()){
    return "products/CreateProduct";
}

//save
MultipartFile image = productDto.getImagefile();
Date createdAt = new Date();
String storageFileName = createdAt.getTime()+"_" + image.getOriginalFilename();
try{
    String uploadDir = "static/";
    Path uploadPath = Paths.get(uploadDir);
    if(!Files.exists(uploadPath)){
        Files.createDirectories(uploadPath);
    }
    try(InputStream inputStream = image.getInputStream()){
        Files.copy(inputStream,Paths.get(uploadDir+storageFileName),StandardCopyOption.REPLACE_EXISTING);

    }
    
}
catch(Exception x){
    System.out.println("Exception" + x.getMessage());
} 
product pro = new product();
pro.setName(productDto.getName());
pro.setBrand(productDto.getBrand());
pro.setCategory(productDto.getCategory());
pro.setPrice(productDto.getPrice());
pro.setDescription(productDto.getDescription());
pro.setCreatedAt(createdAt);
pro.setImageFileName(storageFileName);

repo.save(pro);
return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model,@RequestParam int id){
        try{
        product pro = repo.findById(id).get();
        model.addAttribute("pro",pro);

        productdto proo = new productdto();
proo.setName(pro.getName());
proo.setBrand(pro.getBrand());
proo.setCategory(pro.getCategory());
proo.setPrice(pro.getPrice());
proo.setDescription(pro.getDescription());
model.addAttribute("proo",proo);
        }
        catch(Exception ex){
            System.out.println("Exception" + ex.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }
    @PostMapping("/edit")
    public String updateProduct(Model model,@RequestParam int id,@Valid @ModelAttribute productdto productDto,BindingResult result){
        
        try{
            product pro = repo.findById(id).get();
            model.addAttribute("pro",pro);
            if(result.hasErrors()){
                return "products/EditProduct";
            }
        }catch(Exception ex){
            System.out.println("exception"+ex.getMessage());
        }
        
        
        
        
        return "redirect:/products";
    }
    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id){
        try{
product por = repo.findById(id).get();
Path imagepath = Paths.get("/images/"+por.getImageFileName());
try{
    Files.delete(imagepath);
}catch(Exception ex){
    System.out.println("Exception"+ex.getMessage());
}
repo.delete(por);
        }catch (Exception ex){
            System.out.println("Exception" + ex.getMessage());
        }
        return "redirect:/products";
    }
}
