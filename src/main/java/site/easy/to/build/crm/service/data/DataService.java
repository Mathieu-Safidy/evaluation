package site.easy.to.build.crm.service.data;

import com.opencsv.exceptions.CsvValidationException;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.exception.DataException;

import java.io.IOException;
import java.util.HashMap;

public interface DataService {
    public void multipleImport(MultipartFile customerFiles,
                               MultipartFile ticketFile,
                               MultipartFile budgetFile, Authentication authentication) throws IOException, CsvValidationException, DataException;
    public void importTicketLead(MultipartFile file, HashMap<String, Customer> customerMap,
                                 Authentication authentication) throws IOException, DataException, CsvValidationException;
    public void importBudget(MultipartFile budgetFile, HashMap<String, Customer> customerMap) throws IOException,DataException,CsvValidationException;
    public HashMap<String, Customer> importCustomer(MultipartFile customerFile, Authentication authentication) throws IOException,CsvValidationException,DataException;
    public void importCsv(MultipartFile file) throws Exception;
    public void reset() throws Exception;
    public void restore(MultipartFile file) throws Exception;
}
