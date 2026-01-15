package com.icecreamy.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.icecreamy.entity.Order;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icecreamy.dao.DaoBase;
import com.icecreamy.entity.Customer;
import com.icecreamy.entity.Product;
import com.icecreamy.obj.SignupWebModel;
import com.icecreamy.obj.UserRole;


public abstract class Utilities {

    private static final Logger logg = Logger.getLogger(Utilities.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ModelMapper modelMapper = new ModelMapper();

    static {
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
    }

    public static <T> T convertToOrFromDto(Object convertFrom, Class<T> convertionClass) {
        if (convertFrom == null) return null;
        return modelMapper.map(convertFrom, convertionClass);
    }

    public static <T> List<T> convertListToOrFromDto(List<? extends Serializable> convertFrom, Class<T> convertionClass) {
        if (convertFrom == null) return new ArrayList<>();
        return convertFrom.stream()
                .map(entity -> modelMapper.map(entity, convertionClass))
                .collect(Collectors.toList());
    }

    public static boolean IsValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public static String getSignUpErrors(SignupWebModel signupModel, Customer customerWithSameEmail) {
        String loginError = StringUtils.EMPTY;
        try {
            String firstPass = signupModel.getPassword();
            String secondPass = signupModel.getSecondPassword();
            String email = signupModel.getEmail();
            boolean emailAlreadyExists = customerWithSameEmail != null;
            if (!firstPass.equals(secondPass)) {
                loginError = AppConstants.PASS_DONT_MATCH;
            } else if (firstPass.length() > 12) {
                loginError = AppConstants.PASS_LENGTH_NOT_VALID;
            } else if (!IsValidEmail(email)) {
                loginError = AppConstants.EMAIL_NOT_VALID;
            } else if (emailAlreadyExists) {
                loginError = AppConstants.EMAIL_EXISTS;
            }

            BeanInfo beanInfo = Introspector.getBeanInfo(SignupWebModel.class);
            for (PropertyDescriptor propertyDesc : beanInfo.getPropertyDescriptors()) {
                String propertyName = propertyDesc.getName();
                Object value = propertyDesc.getReadMethod().invoke(signupModel);
                if (StringUtils.isEmpty(value.toString())) {
                    loginError = AppConstants.FIELD_NOT_VALID.replace("{}", propertyName);
                    break;
                }
            }
        } catch (Exception e) {
            logg.error("An error occurred while trying to validate SignupWebModel");
        }
        return loginError;
    }

    public static String getChackOutErrors(Customer customer, List<Product> cartProducts) {
        String chackOutError = StringUtils.EMPTY;
        if (null == customer) {
            chackOutError = AppConstants.PLEASE_LOG_IN;
        } else if (cartProducts.isEmpty()) {
            chackOutError = AppConstants.CART_IS_EMPTY;
        }
        return chackOutError;
    }


    public static void initializeDB(DaoBase<Product> productStub,
                                    DaoBase<Customer> customerStub,
                                    DaoBase<Order> orderStub) {
        try {
            logg.info("Initializing database with CartItem/OrderItem support...");

            File file = new File(AppConstants.DUMMY_PRODUCTS_PATH);
            if (!file.exists()) {
                logg.warn("Dummy products file not found");
                return;
            }

            List<Product> participantJsonList = mapper.readValue(file, new TypeReference<List<Product>>() {
            });
            List<Product> savedProducts = new ArrayList<>();
            for (Product p : participantJsonList) {
                savedProducts.add(productStub.create(p));
            }

            String password = Utilities.encrypt("ivan", "ivan", "ivan@gmail.com");
            Customer ivanCustomer = new Customer("Ivan", "Ivanov", password, "ivan@gmail.com", UserRole.ADMIN);
            ivanCustomer = customerStub.create(ivanCustomer);
            password = Utilities.encrypt("pavlo", "pavlo", "pavlo@gmail.com");
            Customer pavloCustomer = new Customer("Павло", "Павлович", password, "pavlo@gmail.com", UserRole.CUSTOMER);
            pavloCustomer = customerStub.create(pavloCustomer);
            if (!savedProducts.isEmpty()) {
                Product p1 = savedProducts.get(0);
                Order dummyOrder = new Order();
                dummyOrder.setTimestamp(new Date());
                dummyOrder.setCustomer(ivanCustomer);

                com.icecreamy.entity.OrderItem item = new com.icecreamy.entity.OrderItem(dummyOrder, p1, 2);

                List<com.icecreamy.entity.OrderItem> items = new ArrayList<>();
                items.add(item);

                dummyOrder.setItems(items);
                dummyOrder.setAmount(item.getPriceAtPurchase() * item.getQuantity());

                orderStub.create(dummyOrder);
                logg.info("Created dummy order with OrderItems Snapshot");
            }

        } catch (Exception e) {
            logg.error("Failed to initialize dummy data", e);
        }
    }
    public static void initFileSystem() {
        File storageDir = new File(AppConstants.UPLOAD_FILE_PATH);
        if (!storageDir.exists()) {
            boolean created = storageDir.mkdirs();
            System.out.println("Storage created: " + created + " at " + storageDir.getAbsolutePath());
        }
    }
    public static String encrypt(String strToEncrypt, String secretKey, String salt) throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);

        byte[] cipherText = cipher.doFinal(strToEncrypt.getBytes("UTF-8"));
        byte[] encryptedData = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public static String decrypt(String strToDecrypt, String secretKey, String salt) throws Exception {
        byte[] encryptedData = Base64.getDecoder().decode(strToDecrypt);
        byte[] iv = new byte[16];
        System.arraycopy(encryptedData, 0, iv, 0, iv.length);
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);

        byte[] cipherText = new byte[encryptedData.length - 16];
        System.arraycopy(encryptedData, 16, cipherText, 0, cipherText.length);

        byte[] decryptedText = cipher.doFinal(cipherText);
        return new String(decryptedText, "UTF-8");
    }

    public static String convertToJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
