import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Iterator;

import net.lingala.zip4j.core.ZipFile;


import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static org.junit.Assert.assertTrue;

public class UploadFiles {

    private static final String URL_DANCERS_ZIP = "http://hustle-sa.ru/rating/dancers.zip";
    private static final String ZIP_FILE = "dancers/dancers.zip";
    private static final String XLSM_FOLDER = "dancers/";

    // Creating FTP Client instance
    FTPClient ftp = null;

    // Constructor to connect to the FTP Server
    public UploadFiles(String host, int port, String username, String password) throws Exception{

        ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        ftp.connect(host,port);
        System.out.println("FTP URL is:"+ftp.getDefaultPort());
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftp.login(username, password);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }

    // Method to upload the File on the FTP Server
    public void uploadFTPFile(String localFileFullName, String fileName, String hostDir)
            throws Exception
    {
        try {
            InputStream input = new FileInputStream(new File(localFileFullName));

            this.ftp.storeFile(hostDir + fileName, input);
        }
        catch(Exception e){

        }
    }

    // Download the FTP File from the FTP Server
    public void downloadFTPFile(String source, String destination) {
        try (FileOutputStream fos = new FileOutputStream(destination)) {
            this.ftp.retrieveFile(source, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // list the files in a specified directory on the FTP
    public boolean listFTPFiles(String directory, String fileName) throws IOException {
        // lists files and directories in the current working directory
        boolean verificationFilename = false;
        FTPFile[] files = ftp.listFiles(directory);
        for (FTPFile file : files) {
            String details = file.getName();
            System.out.println(details);
            if(details.equals(fileName))
            {
                System.out.println("Correct Filename");
                verificationFilename=details.equals(fileName);
                assertTrue("Verification Failed: The filename is not updated at the CDN end.",details.equals(fileName));
            }
        }

        return verificationFilename;
    }

    // Disconnect the connection to FTP
    public void disconnect(){
        if (this.ftp.isConnected()) {
            try {
                this.ftp.logout();
                this.ftp.disconnect();
            } catch (IOException f) {
                // do nothing as file is already saved to server
            }
        }
    }

    public static void download(String urlString) {
        try {
            URL url = new URL(urlString);
            File file = new File("dancers/dancers.zip");
            FileUtils.copyURLToFile(url, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void unzip(String sourceFile, String destination) {
        try {
            ZipFile zipFile = new ZipFile(sourceFile);
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    public static void convertToXlsx(File inputFile, File outputFile) {
        StringBuffer bf = new StringBuffer();
        FileOutputStream fos = null;
        String strGetValue = "";
        try {
            fos = new FileOutputStream(outputFile);
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(inputFile));
            XSSFSheet sheet = wb.getSheetAt(5);
            Row row;
            Cell cell;
            int intRowCounter = 0;
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                StringBuffer cellDData = new StringBuffer();
                row = rowIterator.next();
                int maxNumOfCells = sheet.getRow(0).getLastCellNum();
                int cellCounter = 0;
                while ((cellCounter) < maxNumOfCells) {
                    if (sheet.getRow(row.getRowNum()) != null
                            && sheet.getRow(row.getRowNum()).getCell(cellCounter) != null) {
                        cell = sheet.getRow(row.getRowNum()).getCell(cellCounter);
                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_BOOLEAN:
                                strGetValue = cell.getBooleanCellValue() + ",";
                                cellDData.append(removeSpace(strGetValue));
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                strGetValue = new BigDecimal(cell.getNumericCellValue()).toPlainString();
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    strGetValue = new DataFormatter().formatCellValue(cell);
                                } else {
                                    strGetValue = new BigDecimal(cell.getNumericCellValue()).toPlainString();
                                }
                                String tempStrGetValue = removeSpace(strGetValue);
                                if (tempStrGetValue.length() == 0) {
                                    strGetValue = " ,";
                                    cellDData.append(strGetValue);
                                } else {
                                    strGetValue = strGetValue + ",";
                                    cellDData.append(removeSpace(strGetValue));
                                }
                                break;
                            case Cell.CELL_TYPE_STRING:
                                strGetValue = cell.getStringCellValue();
                                String tempStrGetValue1 = removeSpace(strGetValue);
                                if (tempStrGetValue1.length() == 0) {
                                    strGetValue = " ,";
                                    cellDData.append(strGetValue);
                                } else {
                                    strGetValue = strGetValue + ",";
                                    cellDData.append(removeSpace(strGetValue));
                                }
                                break;
                            case Cell.CELL_TYPE_BLANK:
                                strGetValue = "" + ",";
                                cellDData.append(removeSpace(strGetValue));
                                break;
                            default:
                                strGetValue = cell + ",";
                                cellDData.append(removeSpace(strGetValue));
                        }
                    } else {
                        strGetValue = " ,";
                        cellDData.append(strGetValue);
                    }
                    cellCounter++;
                }
                String temp = cellDData.toString();
                if (temp.contains(",,,")) {
                    temp = temp.replaceFirst(",,,", ", ,");
                }
                if (temp.endsWith(",")) {
                    temp = temp.substring(0, temp.lastIndexOf(","));
                    cellDData = null;
                    bf.append(temp.trim());
                }
                bf.append("\n");
                intRowCounter++;
            }
            fos.write(bf.toString().getBytes());
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    private static String removeSpace(String strString) {
        if (strString != null && !strString.equals("")) {
            return strString.trim();
        }
        return strString;
    }

    public static void toCSV(Long sheetNumber) {

    }

    // Main method to invoke the above methods
    public static void main(String[] args) {
//        download(URL_DANCERS_ZIP);
//        unzip(ZIP_FILE, XLSM_FOLDER);
        convertToXlsx(new File(XLSM_FOLDER + "/dancers.xlsm"), new File(XLSM_FOLDER + "/dancers.csv"));
//        try {
//            UploadFiles ftpobj = new UploadFiles("radio-hustle.com", 2121, "kir@radio-hustle.com", "R0htkzwbz");
//            ftpobj.uploadFTPFile("ftptest.txt", "ftptest.txt", "/");
//            boolean result = ftpobj.listFTPFiles("/ftptest.txt", "ftptest.txt");
//            System.out.println(result);
//            ftpobj.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}