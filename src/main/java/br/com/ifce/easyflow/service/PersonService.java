package br.com.ifce.easyflow.service;

import br.com.ifce.easyflow.controller.dto.address.AddressRequestDTO;
import br.com.ifce.easyflow.controller.dto.person.PersonCreateDTO;
import br.com.ifce.easyflow.controller.dto.person.PersonDTO;
import br.com.ifce.easyflow.controller.dto.security.PersonSecurityDTO;
import br.com.ifce.easyflow.controller.dto.security.TokenDTO;
import br.com.ifce.easyflow.controller.dto.user.UserRequestDTO;
import br.com.ifce.easyflow.controller.dto.user.UserResponseDTO;
import br.com.ifce.easyflow.exception.PersonNotFoundException;
import br.com.ifce.easyflow.model.Address;
import br.com.ifce.easyflow.model.Course;
import br.com.ifce.easyflow.model.Person;
import br.com.ifce.easyflow.model.StudyArea;
import br.com.ifce.easyflow.model.User;
import br.com.ifce.easyflow.repository.PersonRepository;
import br.com.ifce.easyflow.repository.UserRepository;
import br.com.ifce.easyflow.security.TokenService;
import br.com.ifce.easyflow.service.exceptions.BadRequestException;
import br.com.ifce.easyflow.service.exceptions.ConflictException;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final UserService userService;
    private final CourseService courseService;
    private final StudyAreaService studyAreaService;
    private final UserRepository userRepository;
    private final AddressService addressService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private TokenService tokenService;

    public PersonService(PersonRepository personRepository, UserService userService, AddressService addressService, CourseService courseService, StudyAreaService studyAreaService,UserRepository userRepository) {
        this.personRepository = personRepository;
        this.userService = userService;
        this.courseService = courseService;
        this.studyAreaService = studyAreaService;
        this.addressService = addressService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Person save(Person person){
        return this.personRepository.save(person);
    }

    public List<Person> findAll(){
        return this.personRepository.findAll();
    }
    public List<Person> findAllRepresentants() {
        List<Person> allPersons = this.personRepository.findAll();
        List<Person> representants = new ArrayList<>();
    
        for (Person person : allPersons) {
            if (person.isPerson_representant()) {
                representants.add(person);
            }
        }
    
        return representants;
    }
    

    @Transactional
    public Person update(Long id, PersonDTO personDTO){
        Person oldPerson = this.findById(id);
        User oldUser = userService.findByLogin(oldPerson.getEmail());


        if(!Objects.equals(oldPerson.getEmail(), personDTO.getEmail()) && this.existsByEmail(personDTO.getEmail())){
            throw new ConflictException("The email provided is already being used.");
        }

        StudyArea studyArea = studyAreaService.searchByID(personDTO.getStudy_area_id());
        Course course = courseService.searchByID(personDTO.getCourse_id());

        oldPerson.setName(personDTO.getName());
        oldPerson.setEmail(personDTO.getEmail());
        oldPerson.setCourse(course);
        oldPerson.setStudy_area(studyArea);
        oldUser.setLogin(personDTO.getEmail());

        userRepository.save(oldUser);

       return this.save(oldPerson);
    }


    @Transactional
    public Boolean delete(Long id){
        Person person = this.findById(id);

        if(person != null){
            this.personRepository.delete(person);
            return true;
        }
        return false;
    }

    public Person findById(Long id){
        return this.personRepository.findById(id)
                .orElseThrow(PersonNotFoundException::new);
    }
    @Transactional
    public boolean existsById(Long id) {
        Optional<Person> exist = this.personRepository.findById(id);
        return exist.isPresent();
    }

    @Transactional
    public boolean existsByEmail(String email) {
        Optional<Person> exist = this.personRepository.findByEmail(email);
        return exist.isPresent();
    }

    public Person turnIntoRepresentative(Long id) {
        Person person = this.findById(id);
        person.setPerson_representant(true);
        return personRepository.save(person);
    }

    @Transactional
    public PersonSecurityDTO createPerson(PersonCreateDTO personCreateDTO, AddressRequestDTO addressRequestDTO) {

            if (existsByEmail(personCreateDTO.getEmail())) {
                throw new ConflictException("The email provided is already being used.");
            }

            if(!personCreateDTO.getPassword().equals(personCreateDTO.getRepeated_password())){
                throw new BadRequestException("Passwords does not match.");
            }

            UserRequestDTO newUserDTO = new UserRequestDTO(personCreateDTO.getEmail(), personCreateDTO.getPassword());
            User user = userService.save(newUserDTO);
        
            Person person = new Person();
            BeanUtils.copyProperties(personCreateDTO, person);
            person.setUser(user);
            person.setCourse(courseService.searchByID(personCreateDTO.getCourse_id()));
            person.setStudy_area(studyAreaService.searchByID(personCreateDTO.getStudy_area_id()));
            Address address = addressService.createAddress(addressRequestDTO);
            address = addressService.save(address); 
            person.setAddresses(address);
            person = this.save(person);
            user.setPerson(person);
   
            UsernamePasswordAuthenticationToken login = new UsernamePasswordAuthenticationToken(personCreateDTO.getEmail(), personCreateDTO.getPassword());

            Authentication authentication = authManager.authenticate(login);
            TokenDTO token = new TokenDTO(tokenService.generateToken(authentication));
            UserResponseDTO userResponseDTO = new UserResponseDTO((User) authentication.getPrincipal());

            userResponseDTO.setPersonDTO(person);    

            return new PersonSecurityDTO(token, userResponseDTO);
    }
}
