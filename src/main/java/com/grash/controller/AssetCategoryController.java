package com.grash.controller;

import com.grash.dto.CategoryPatchDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.model.AssetCategory;
import com.grash.model.CompanySettings;
import com.grash.model.User;
import com.grash.service.AssetCategoryService;
import com.grash.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/asset-categories")
@Api(tags = "assetCategory")
@RequiredArgsConstructor
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;
    private final UserService userService;


    @GetMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "AssetCategory not found")})
    public Collection<AssetCategory> getAll(HttpServletRequest req) {
        User user = userService.whoami(req);
        CompanySettings companySettings = user.getCompany().getCompanySettings();
        return assetCategoryService.findByCompanySettings(companySettings.getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "AssetCategory not found")})
    public AssetCategory getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        User user = userService.whoami(req);
        Optional<AssetCategory> optionalAssetCategory = assetCategoryService.findById(id);
        if (optionalAssetCategory.isPresent()) {
            if (assetCategoryService.hasAccess(user, optionalAssetCategory.get())) {
                return assetCategoryService.findById(id).get();
            } else {
                throw new CustomException("Can't get assetCategory from other company", HttpStatus.NOT_ACCEPTABLE);
            }
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public AssetCategory create(@ApiParam("AssetCategory") @RequestBody AssetCategory assetCategoryReq, HttpServletRequest req) {
        User user = userService.whoami(req);
        if (assetCategoryService.canCreate(user, assetCategoryReq)) {
            return assetCategoryService.create(assetCategoryReq);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "AssetCategory not found")})
    public AssetCategory patch(@ApiParam("AssetCategory") @RequestBody CategoryPatchDTO assetCategory,
                               @ApiParam("id") @PathVariable("id") Long id,
                               HttpServletRequest req) {
        User user = userService.whoami(req);
        Optional<AssetCategory> optionalAssetCategory = assetCategoryService.findById(id);

        if (optionalAssetCategory.isPresent()) {
            AssetCategory savedAssetCategory = optionalAssetCategory.get();
            if (assetCategoryService.hasAccess(user, savedAssetCategory) && assetCategoryService.canPatch(user, assetCategory)) {
                return assetCategoryService.update(id, assetCategory);
            } else {
                throw new CustomException("Can't patch assetCategory from other company", HttpStatus.NOT_ACCEPTABLE);
            }
        } else {
            return null;
        }


    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "AssetCategory not found")})
    public ResponseEntity delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        User user = userService.whoami(req);

        Optional<AssetCategory> optionalAssetCategory = assetCategoryService.findById(id);
        if (optionalAssetCategory.isPresent()) {
            if (assetCategoryService.hasAccess(user, optionalAssetCategory.get())) {
                assetCategoryService.delete(id);
                return new ResponseEntity(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("AssetCategory not found", HttpStatus.NOT_FOUND);
    }

}
