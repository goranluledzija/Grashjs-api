package com.grash.service;

import com.grash.dto.PartPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.PartMapper;
import com.grash.model.*;
import com.grash.model.enums.NotificationType;
import com.grash.model.enums.RoleType;
import com.grash.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartService {
    private final PartRepository partRepository;
    private final FileService fileService;
    private final PartConsumptionService partConsumptionService;
    private final CompanyService companyService;
    private final LocationService locationService;
    private final PartMapper partMapper;
    private final EntityManager em;
    private final NotificationService notificationService;

    @Transactional
    public Part create(Part Part) {
        Part savedPart = partRepository.saveAndFlush(Part);
        em.refresh(savedPart);
        return savedPart;
    }

    @Transactional
    public Part update(Long id, PartPatchDTO part) {
        if (partRepository.existsById(id)) {
            Part savedPart = partRepository.findById(id).get();
            Part patchedPart = partRepository.saveAndFlush(partMapper.updatePart(savedPart, part));
            em.refresh(patchedPart);
            return patchedPart;

        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public void consumePart(Long id, int quantity, Company company, WorkOrder workOrder) {
        Part part = findById(id).get();
        if (part.getQuantity() >= quantity) {
            part.setQuantity(part.getQuantity() - quantity);
            if (part.getQuantity() < part.getMinQuantity()) {
                part.getAssignedTo().forEach(user ->
                        notificationService.create(new Notification(part.getName() + " is getting Low", user, NotificationType.PART, part.getId()))
                );
            }
            partConsumptionService.create(new PartConsumption(company, part, workOrder, quantity));
            partRepository.save(part);
        } else throw new CustomException("There is not enough of this part", HttpStatus.NOT_ACCEPTABLE);
    }

    public Collection<Part> getAll() {
        return partRepository.findAll();
    }

    public void delete(Long id) {
        partRepository.deleteById(id);
    }

    public Optional<Part> findById(Long id) {
        return partRepository.findById(id);
    }

    public Collection<Part> findByCompany(Long id) {
        return partRepository.findByCompany_Id(id);
    }

    public boolean hasAccess(OwnUser user, Part part) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(part.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, Part partReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(partReq.getCompany().getId());

        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);

        return first && canPatch(user, partMapper.toPatchDto(partReq));
    }

    public boolean canPatch(OwnUser user, PartPatchDTO partReq) {
        Long companyId = user.getCompany().getId();

        Optional<File> optionalImage = partReq.getImage() == null ? Optional.empty() : fileService.findById(partReq.getImage().getId());
        Optional<Location> optionalLocation = partReq.getLocation() == null ? Optional.empty() : locationService.findById(partReq.getLocation().getId());

        boolean third = partReq.getImage() == null || (optionalImage.isPresent() && optionalImage.get().getCompany().getId().equals(companyId));
        boolean fourth = partReq.getLocation() == null || (optionalLocation.isPresent() && optionalLocation.get().getCompany().getId().equals(companyId));

        return third && fourth;
    }

    public void notify(Part part) {
        String message = "Part " + part.getName() + " has been assigned to you";
        part.getUsers().forEach(user -> notificationService.create(notificationService.create(new Notification(message, user, NotificationType.PART, part.getId()))));
    }

    public void patchNotify(Part oldPart, Part newPart) {
        String message = "Part " + newPart.getName() + " has been assigned to you";
        oldPart.getNewUsersToNotify(newPart.getUsers()).forEach(user -> notificationService.create(
                new Notification(message, user, NotificationType.PART, newPart.getId())));
    }

    public Part save(Part part) {
        return partRepository.save(part);
    }
}
