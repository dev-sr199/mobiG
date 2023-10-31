package com.ecommerce.project.service;

import com.ecommerce.project.entity.Address;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public List<Address> getAddressesForUser(User user) {
        return addressRepository.findAllByUser(user);
    }

    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    public void deleteAddressById(Long addressId) {
        addressRepository.deleteById(addressId);
    }

    public Address getAddressById(Long selectedAddressId) {
        return addressRepository.findById(selectedAddressId).get();
    }

}
