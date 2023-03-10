package br.com.ifce.easyflow.service;

import br.com.ifce.easyflow.controller.dto.person.PersonCreateDTO;
import br.com.ifce.easyflow.model.Person;
import br.com.ifce.easyflow.model.StudyArea;
import br.com.ifce.easyflow.model.User;
import br.com.ifce.easyflow.repository.PersonRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final UserService userService;
    private final CourseService courseService;

    public PersonService(PersonRepository personRepository, UserService userService, CourseService courseService) {
        this.personRepository = personRepository;
        this.userService = userService;
        this.courseService = courseService;
    }

    @Transactional
    public Person save(Person person){

        return this.personRepository.save(person);
    }

    public List<Person> findAll(){
        return this.personRepository.findAll();
    }



    @Transactional
    public Optional<Person> update(Person newPerson){
        Optional<Person> oldPerson = this.findById(newPerson.getId());

        return oldPerson.isPresent()
                ? Optional.of(this.save(newPerson))
                : Optional.empty();
    }


    @Transactional
    public Boolean delete(Long id){
        Optional<Person> person = this.personRepository.findById(id);

        if(person.isPresent()){
            this.personRepository.delete(person.get());
            return true;
        }

        return false;
    }

    public Optional<Person> findById(Long id){
        return this.personRepository.findById(id);
    }

    public boolean existsById(Long id) {
        Optional<Person> exist = this.personRepository.findById(id);
        return exist.isPresent();
    }


    public boolean existsByEmail(String email) {
        Optional<Person> exist = this.personRepository.findByEmail(email);

        return exist.isPresent();
    }

    public Person createPerson(PersonCreateDTO personCreateDTO) {
        User user = new User(
                personCreateDTO.getEmail(),
                new BCryptPasswordEncoder().encode(personCreateDTO.getPassword())
        );
        userService.save(user);
        Person person = new Person();
        BeanUtils.copyProperties(personCreateDTO, person);
        person.setUser(user);
        person.setCourse(courseService.searchByID(personCreateDTO.getCourse_id()).get());
        person.setStudy_area(new StudyArea(personCreateDTO.getName()));
        this.save(person);
        return person;
    }
}