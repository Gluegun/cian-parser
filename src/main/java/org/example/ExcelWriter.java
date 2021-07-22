package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.java.Log;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Log
public class ExcelWriter {

    public static void writeToFile(String directory, List<Apartment> apartments) throws IOException {

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Apartments");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);

        Row header = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        setUpHeaderCellStyle(workbook, headerStyle);

        createCell(header, 0, "Id", headerStyle);
        createCell(header, 1, "Адрес", headerStyle);
        createCell(header, 2, "Этаж", headerStyle);
        createCell(header, 3, "Кол-во комнат", headerStyle);
        createCell(header, 4, "Площадь", headerStyle);
        createCell(header, 5, "Цена", headerStyle);
        createCell(header, 6, "Ссылка", headerStyle);
        createCell(header, 7, "Конкурент", headerStyle);

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);

        int rowNumber = 1;
        for (int i = 0; i < apartments.size(); i++) {
            Apartment apartment = apartments.get(i);
            Row row = sheet.createRow(rowNumber++);
            String competitorInfo = apartment.getAuthor() + " " + apartment.getMark() + " " + apartment.getPhoneNumber();
            createCell(row, 0, apartment.getId(), style);
            createCell(row, 1, apartment.getAddress(), style);
            createCell(row, 2, apartment.getFlour(), style);
            createCell(row, 3, String.valueOf(apartment.getAmountOfRooms()), style);
            createCell(row, 4, String.valueOf(apartment.getSqr()), style);
            createCell(row, 5, String.valueOf(apartment.getPrice()), style);
            createCell(row, 6, apartment.getLink(), style);
            createCell(row, 7, competitorInfo, style);
        }
        Path path = Paths.get(directory, "/apartments.xlsx");

        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        FileOutputStream outputStream = new FileOutputStream(path.toString());
        workbook.write(outputStream);
        outputStream.flush();
        workbook.close();
        outputStream.close();

        log.info("excel file successfully created and filled");
    }

    private static Cell createCell(Row row, int columnNumber, String value, CellStyle style) {
        Cell headerCell = row.createCell(columnNumber);
        headerCell.setCellValue(value);
        headerCell.setCellStyle(style);
        return headerCell;
    }

    private static void setUpHeaderCellStyle(XSSFWorkbook workbook, CellStyle headerStyle) {
        headerStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setWrapText(true);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        headerStyle.setFont(font);
    }

}
