package kopo.newproject.util;

public class CreatePassword {
    public static String createTempPassword() {
        int length = 10;
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randIndex = (int) (Math.random() * charSet.length());
            password.append(charSet.charAt(randIndex));
        }

        return password.toString();
    }

}
