package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel 報表工具類
 */
@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelUtil {

	/**
	 * 匯入資料至工作表
	 * 
	 * @param sheet
	 * @param headerList 標題列資料
	 * @param dataList   資料內容
	 * @return InputStreamResource
	 */
	public static InputStreamResource exportDataAsResource(List<String> headerList, List<Object> dataList) {
		// 處理標題及內容資料
		XSSFWorkbook book = processWorkbook(headerList, dataList);

		// 建立 Resource 往前端送
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
			book.write(bos);
			byte[] bookByteArray = bos.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(bookByteArray);
			book.close();
			return new InputStreamResource(bis);

		} catch (IOException e) {
			log.error("轉換錯誤，產生報表失敗", e);
			return null;
		}
	}

	/**
	 * 建立 Excel 表單資料
	 * 
	 * @param headerList 標題列資料
	 * @param dataList   資料內容
	 */
	public static XSSFWorkbook processWorkbook(List<String> headerList, List<? extends Object> dataList) {
		// 新建工作簿
		XSSFWorkbook book = new XSSFWorkbook();
		// 建立工作表
		XSSFSheet sheet = book.createSheet("Books");

		Object[] headers = headerList.toArray();

		// 資料轉換
		List<Object[]> dataset = new ArrayList<>();
		dataList.stream().forEach(e -> dataset.add(convertObjectToArray(e)));

		// 匯入資料至工作表
		importData(sheet, headers, dataset);

		return book;
	}

	/**
	 * 匯入資料至工作表
	 * 
	 * @param sheet
	 * @param headerList 標題列資料
	 * @param dataset    資料內容
	 * @return InputStreamResource
	 */
	public static byte[] exportDataAsByteArray(List<String> headerList, List<? extends Object> dataList) {
		// 處理標題及內容資料
		XSSFWorkbook book = processWorkbook(headerList, dataList);

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
			book.write(bos);
			book.close();
			return bos.toByteArray();

		} catch (IOException e) {
			log.error("轉換錯誤，產生報表失敗", e);
			return new byte[0];
		}
	}

	/**
	 * 匯入資料至工作表
	 *
	 * @param sheet   工作表
	 * @param header  標頭 (可為 null)
	 * @param dataset 資料集
	 */
	public static void importData(XSSFSheet sheet, Object[] header, List<Object[]> dataset) {
		int rowIdx = 0;

		// 創建表頭 (不修改 dataset 原始資料)
		if (header != null) {
			writeRow(sheet, rowIdx++, header);
		}

		// 處理資料
		for (Object[] data : dataset) {
			writeRow(sheet, rowIdx++, data);
		}
	}

	/**
	 * 將一列數據寫入 Excel Sheet
	 * 
	 * @param sheet   工作表
	 * @param rowIdx  index
	 * @param rowData
	 */
	private static void writeRow(XSSFSheet sheet, int rowIdx, Object[] rowData) {
		XSSFRow row = sheet.createRow(rowIdx);

		// 使用 forEachIndexed 模擬索引
		IntStream.range(0, rowData.length).forEach(colIdx -> {
			XSSFCell cell = row.createCell(colIdx);
			setCellValue(cell, rowData[colIdx]);
		});
	}

	/**
	 * 設定 Excel Cell 值 (使用 Map 類型判斷)
	 * 
	 * @param cell Cell 資料
	 * @param 類型
	 */
	private static void setCellValue(XSSFCell cell, Object field) {
		if (field == null) {
			cell.setCellValue("");
			return;
		}

		// 類型對應處理 (函數式 Map)
		Map<Class<?>, BiConsumer<XSSFCell, Object>> typeHandler = Map.of(String.class,
				(c, v) -> c.setCellValue((String) v), Integer.class, (c, v) -> c.setCellValue((Integer) v), Long.class,
				(c, v) -> c.setCellValue((Long) v), Double.class, (c, v) -> c.setCellValue((Double) v), Date.class,
				(c, v) -> c.setCellValue(DateFormatUtils.format((Date) v, "yyyy/MM/dd")), BigDecimal.class,
				(c, v) -> c.setCellValue(((BigDecimal) v).doubleValue()));

		// 根據類型設定值
		typeHandler.getOrDefault(field.getClass(), (c, v) -> c.setCellValue("")).accept(cell, field);

	}

	/**
	 * 讀取 Excel 單一 sheet資料
	 * 
	 * @param inputStream : 資料流
	 * @param sheetName   : 工作表名稱
	 * @return List<map<String, String>>: List<map<標題, 值>> 一個 Map 是一列資料
	 * @throws IOException
	 */
	public static List<Map<String, String>> readExcelData(InputStream inputStream, String sheetName)
			throws IOException {
		List<Map<String, String>> result = new ArrayList<>();
		Workbook workbook = new XSSFWorkbook(inputStream);
		processWorkbook(workbook, sheetName, result);
		workbook.close(); // 關閉工作簿以釋放資源
		return result;
	}

	/**
	 * 處理 多個 sheet
	 * 
	 * @param Workbook  workbook
	 * @param sheetName 表名
	 * @param 處理後的資料
	 */
	private static void processWorkbook(Workbook workbook, String sheetName, List<Map<String, String>> result) {

		Sheet sheet = workbook.getSheet(sheetName);

		// 獲取第一列（標題列）
		Row titleRow = sheet.getRow(0);

		// 迭代列 (從第 2 列開始)
		for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			Map<String, String> map = new HashMap<>();
			// 資料列
			Row row = sheet.getRow(rowIndex);
			if (row == null) {
				break;
			}

			// 遍歷標題行的每一個單元格
			Iterator<Cell> titleCellIterator = titleRow.cellIterator();
			int cellIndex = 0;
			while (titleCellIterator.hasNext()) {
				String key = StringUtils.trim(parseCellValue(titleCellIterator.next()));
				String value = parseCellValue(row.getCell(cellIndex));
				if (StringUtils.isNotBlank(key)) {
					map.put(key, value);
				}
				cellIndex++;
			}
			log.info("map: {}", map);
			result.add(map);
		}

	}

	/**
	 * 讀取多張表資料
	 * 
	 * @throws IOException
	 */
	public static Map<String, List<Map<String, String>>> readExcelData(InputStream inputStream,
			List<String> sheetNameList) throws IOException {

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		Workbook workbook = new XSSFWorkbook(inputStream);

		sheetNameList.stream().forEach(sheetName -> {
			log.debug("Sheet Name:{}", sheetName);
			List<Map<String, String>> list = new ArrayList<>();
			processWorkbook(workbook, sheetName, list);
			result.put(sheetName, list);
		});

		return result;

	}

	/**
	 * 轉換單元格內的值
	 */
	private static String parseCellValue(Cell cell) {
		String cellValue = "";
		switch (cell.getCellType()) {
		case STRING:
			cellValue = cell.getStringCellValue();
			break;
		case NUMERIC:
			cellValue = String.valueOf(cell.getNumericCellValue());
			break;
		case BOOLEAN:
			cellValue = String.valueOf(cell.getBooleanCellValue());
			break;
		case BLANK:
			break;
		default:
			break;
		}

		return cellValue;
	}

	/**
	 * 動態轉換物件為 Object[]
	 * 
	 * @param Class
	 * @return Object[]
	 */
	private static Object[] convertObjectToArray(Object obj) {
		Class<?> clazz = obj.getClass();
		Field[] fields = clazz.getDeclaredFields();
		Object[] objectArray = new Object[fields.length];

		try {
			for (int i = 0; i < fields.length; i++) {
//				fields[i].setAccessible(true);
				ReflectionUtils.makeAccessible(fields[i]);
				objectArray[i] = fields[i].get(obj);
			}
		} catch (IllegalAccessException e) {
			log.error("轉換發生錯誤:", e);
		}
		return objectArray;
	}

	/**
	 * 本地端下載
	 * 
	 * @param book
	 * @param path 檔案下載路徑
	 */
	public static void downloadLocal(XSSFWorkbook book, String path) {
		try (FileOutputStream os = new FileOutputStream(path)) {
			book.write(os);
		} catch (FileNotFoundException e) {
			log.error("File Not Found ", e);
		} catch (IOException e) {
			log.error("轉換錯誤");
		}
	}

}
