package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.ProductNotFoundException;
import dev.kmfg.flooring.dto.Product;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        final BigDecimal costPerSqft = new BigDecimal(productParts[1]).setScale(2, RoundingMode.HALF_UP);
        final BigDecimal laborCostPerSqft = new BigDecimal(productParts[2]).setScale(2, RoundingMode.HALF_UP);

        return new Product(productType, costPerSqft, laborCostPerSqft);
    }

    private String marshallProduct(Product product) throws FlooringDataPersistenceException {
        if(product == null) {
            throw new FlooringDataPersistenceException("Cannot marshall a null product!");
        } else if(product.getProductType() == null ||
                product.getCostPerSqft() == null ||
                product.getLaborCostPerSqft() == null) {
            throw new FlooringDataPersistenceException("Cannot marshall a product with null parts!");
        }

        return Stream.of(
                    product.getProductType(),
                    product.getCostPerSqft(),
                    product.getLaborCostPerSqft()
                )
                .map(Objects::toString)
                .collect(Collectors.joining(DELIMITER));
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
