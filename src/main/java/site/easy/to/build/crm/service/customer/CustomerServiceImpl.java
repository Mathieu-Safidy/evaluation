package site.easy.to.build.crm.service.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.service.lead.LeadServiceImpl;
import site.easy.to.build.crm.service.ticket.TicketServiceImpl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final LeadServiceImpl leadServiceImpl;
    private final TicketServiceImpl ticketServiceImpl;

    public CustomerServiceImpl(CustomerRepository customerRepository, LeadServiceImpl leadServiceImpl, TicketServiceImpl ticketServiceImpl) {
        this.customerRepository = customerRepository;
        this.leadServiceImpl = leadServiceImpl;
        this.ticketServiceImpl = ticketServiceImpl;
    }

    @Override
    public Customer findByCustomerId(int customerId) {
        return customerRepository.findByCustomerId(customerId);
    }

    @Override
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public List<Customer> findByUserId(int userId) {
        return customerRepository.findByUserId(userId);
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public void delete(Customer customer) {
        customerRepository.delete(customer);
    }

    @Override
    public List<Customer> getRecentCustomers(int userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return customerRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long countByUserId(int userId) {
        return customerRepository.countByUserId(userId);
    }

    public void duplicate(Customer customer,String lien) throws IOException {
        List<Lead> leadsCustomer = leadServiceImpl.findByCustomerId(customer.getCustomerId());
        List<Ticket> ticketsCustomer = ticketServiceImpl.findCustomerTickets(customer.getCustomerId());

        try {
            writevalueCustomer(lien, customer, leadsCustomer, ticketsCustomer);
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("Il y a eu une erreur sur l'ecriture du fichier"+lien);
        }
    }

    public void writevalueCustomer(String lien,Customer customer,List<Lead> leadlist,List<Ticket> ticketList) throws Exception{
        String customerName = customer.getName()+" copy";
        String customerEmail = "copy_"+customer.getEmail();
        customer.setName(customerName);
        customer.setEmail(customerEmail);
        Map<String,Object> map = new HashMap<>();
        map.put("customer",customer);
        map.put("lead",leadlist);
        map.put("ticketList",ticketList);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(map);

//        String separateur = ",";
//        String donneCustomer = customerName+separateur+customerEmail;
//        String allData = "";
//
//        for (Lead lead : leadlist) {
//            allData += allData+donneCustomer+separateur+"lead"+separateur+lead.getStatus()+separateur+lead.getDepenses().getMontant()+separateur+lead.getCreatedAt()+"\n";
//        }
//        for (Ticket ticket : ticketList) {
//            allData += allData+donneCustomer+separateur+"ticket"+separateur+ticket.getStatus()+separateur+ticket.getDepenses().getMontant()+separateur+ticket.getCreatedAt()+"\n";
//        }


        writevalue(lien,json);

    }
    public void writevalue(String lien, String text){
        Path path = Paths.get(lien);
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
        {
            bw.write(text);
            System.out.println("Successfully written data to the file");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
