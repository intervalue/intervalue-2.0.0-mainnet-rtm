package one.inve.util;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * 写Excel文件工具包
 * Created by Clare  on 2018/7/7 0007.
 */
public class ExcelUtils {
    private String excelPath;
    private Workbook workbook;
    private Sheet sheet1;

    /**使用栗子
     * WriteExcel excel = new WriteExcel("D:\\myexcel.xlsx");
     * excel.write(new String[]{"1","2"}, 0);//在第1行第1个单元格写入1,第一行第二个单元格写入2
     */
    public void write(String[] writeStrings, int rowNumber) throws Exception {
        //将内容写入指定的行号中
        Row row = sheet1.createRow(rowNumber);
        //遍历整行中的列序号
        for (int j = 0; j < writeStrings.length; j++) {
            //根据行指定列坐标j,然后在单元格中写入数据
            Cell cell = row.createCell(j);
            cell.setCellValue(writeStrings[j]);
        }
        OutputStream stream = new FileOutputStream(excelPath);
        workbook.write(stream);
        stream.close();
    }

    public void write(double[] data, int rowNumber) throws Exception {
        //将内容写入指定的行号中
        Row row = sheet1.createRow(rowNumber);
        //遍历整行中的列序号
        for (int j = 0; j < data.length; j++) {
            //根据行指定列坐标j,然后在单元格中写入数据
            Cell cell = row.createCell(j);
            cell.setCellValue(data[j]);
        }
        OutputStream stream = new FileOutputStream(excelPath);
        workbook.write(stream);
        stream.close();
    }

    public void write(double[] data, int rowNumber, String rowName) throws Exception {
        //将内容写入指定的行号中
        Row row = sheet1.createRow(rowNumber);
        Cell cell = row.createCell(0);
        cell.setCellValue(rowName);
        //遍历整行中的列序号
        for (int j = 0; j < data.length; j++) {
            //根据行指定列坐标j,然后在单元格中写入数据
            cell = row.createCell(j+1);
            cell.setCellValue(data[j]);
        }
        OutputStream stream = new FileOutputStream(excelPath);
        workbook.write(stream);
        stream.close();
    }

    public ExcelUtils(String excelPath) throws Exception {
        this.excelPath = excelPath;
        File excelFile = new File(excelPath);
        if (excelFile.exists()) {
            excelFile.delete();
        }
        String fileType = excelPath.substring(excelPath.lastIndexOf(".") + 1, excelPath.length());
        //创建文档对象
        if (fileType.equals("xls")) {
            //如果是.xls,就new HSSFWorkbook()
            workbook = new HSSFWorkbook();
        } else if (fileType.equals("xlsx")) {
            //如果是.xlsx,就new XSSFWorkbook()
            workbook = new XSSFWorkbook();
        } else {
            throw new Exception("文档格式后缀不正确!!！");
        }
        // 创建表sheet
        sheet1 = workbook.createSheet("sheet1");
    }
}



