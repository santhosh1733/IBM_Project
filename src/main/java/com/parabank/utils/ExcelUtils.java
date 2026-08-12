package com.parabank.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Apache POI wrapper for reading test data from Excel (.xlsx) and writing
 * results back. Used for data-driven tests via TestNG @DataProvider.
 *
 * Expected Excel layout: row 0 = headers, each subsequent row = one test data set.
 * Example sheet "RequestLoan":
 *   | TestCaseId  | LoanAmount | DownPayment | ExpectedStatus |
 *   | TC_LOAN_F02 | 5000       | 2000        | Approved       |
 *   | TC_LOAN_F03 | 50000      | 10          | Denied         |
 */
public class ExcelUtils {

    /**
     * Reads a sheet into a List of Maps: each Map is one row, keyed by column header.
     * This is the format most convenient for feeding a TestNG @DataProvider.
     */
    public static List<Map<String, String>> getSheetData(String filePath, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in " + filePath);
            }

            Row headerRow = sheet.getRow(0);
            int colCount = headerRow.getLastCellNum();

            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int col = 0; col < colCount; col++) {
                    String header = getCellValueAsString(headerRow.getCell(col));
                    String value = getCellValueAsString(row.getCell(col));
                    rowData.put(header, value);
                }
                data.add(rowData);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }

        return data;
    }

    /**
     * Converts sheet data directly into Object[][] for @DataProvider consumption.
     * Each row becomes a single Object[] containing one Map<String,String>.
     */
    public static Object[][] getDataProviderArray(String filePath, String sheetName) {
        List<Map<String, String>> rows = getSheetData(filePath, sheetName);
        Object[][] result = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            result[i][0] = rows.get(i);
        }
        return result;
    }

    /** Writes actual results back to a new column, useful for audit trail / reporting. */
    public static void writeResult(String filePath, String sheetName, int rowIndex,
                                    String columnHeader, String value) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            Row headerRow = sheet.getRow(0);

            int colIndex = -1;
            for (Cell cell : headerRow) {
                if (cell.getStringCellValue().equalsIgnoreCase(columnHeader)) {
                    colIndex = cell.getColumnIndex();
                    break;
                }
            }
            if (colIndex == -1) {
                colIndex = headerRow.getLastCellNum();
                headerRow.createCell(colIndex).setCellValue(columnHeader);
            }

            Row row = sheet.getRow(rowIndex + 1);
            if (row == null) row = sheet.createRow(rowIndex + 1);
            row.createCell(colIndex).setCellValue(value);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to write to Excel file: " + filePath, e);
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
