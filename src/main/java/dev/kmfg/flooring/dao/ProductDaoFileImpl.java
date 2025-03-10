package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.ProductNotFoundException;
import dev.kmfg.flooring.model.Product;
import dev.kmfg.flooring.service.validator.GenericValidator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.*;

public class ProductDaoFileImpl implements ProductDao {
    private static final String DELIMITER = ",";
    private static final int REQUIRED_PARTS = 3;

    private String fileName = "Data/Products.txt";
    private HashMap<String, Product> products;

    public ProductDaoFileImpl(String fileName) {
        this.fileName = fileName;
        this.products = new HashMap<>();
    }

    public ProductDaoFileImpl() {
        this.products = new HashMap<>();
    }

    private Product unmarshallProduct(String productStr) throws FlooringDataPersistenceException {
        final String[] productParts = productStr.split(DELIMITER);
        if(productParts.length != REQUIRED_PARTS) {
            throw new FlooringDataPersistenceException(
                    String.format(
                            "Malformed data while unmarshalling product. Required %d parts, but received %d.",
                            REQUIRED_PARTS,
                            productParts.length
                    )
            );
        }

        final String productType = productParts[0];
        final BigDecimal costPerSqft = GenericValidator.createBigDecimal(productParts[1]);
        final BigDecimal laborCostPerSqft = GenericValidator.createBigDecimal(productParts[2]);

        return new Product(productType, costPerSqft, laborCostPerSqft);
    }

    private void read() throws FlooringDataPersistenceException {
        Scanner scanner;

        try {
            scanner = new Scanner(
                    new BufferedReader(
                            new FileReader(fileName)));
        } catch (FileNotFoundException e) {
            throw new FlooringDataPersistenceException(
                    "Could not load product data into memory.", e);
        }

        products = new HashMap<>();

        String currentLine;
        Product product;
        if(scanner.hasNextLine()) {
            scanner.nextLine(); // consume the CSV header.
        }
        while (scanner.hasNextLine()) {
            currentLine = scanner.nextLine();
            product = unmarshallProduct(currentLine);
            products.put(product.getProductType(), product);
        }

        scanner.close();
    }

    @Override
    public Product getProduct(String productType) throws FlooringDataPersistenceException, ProductNotFoundException {
        read();

        if(products.containsKey(productType)) {
            return products.get(productType);
        } else {
            throw new ProductNotFoundException(String.format("No product was found for the type %s", productType));
        }
    }

    @Override
    public List<Product> getAllProducts() throws FlooringDataPersistenceException {
        read();

        return new ArrayList<>(products.values());
    }
}
