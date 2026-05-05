package com.project.DuAnTotNghiep.service.serviceImpl;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.project.DuAnTotNghiep.dto.Bill.*;
import com.project.DuAnTotNghiep.dto.CustomerDto.CustomerDto;
import com.project.DuAnTotNghiep.entity.*;
import com.project.DuAnTotNghiep.entity.enumClass.BillStatus;
import com.project.DuAnTotNghiep.entity.enumClass.InvoiceType;
import com.project.DuAnTotNghiep.exception.NotFoundException;
import com.project.DuAnTotNghiep.repository.BillRepository;
import com.project.DuAnTotNghiep.repository.ProductDetailRepository;
import com.project.DuAnTotNghiep.repository.ProductRepository;
import com.project.DuAnTotNghiep.repository.Specification.BillSpecification;
import com.project.DuAnTotNghiep.repository.Specification.ProductSpecification;
import com.project.DuAnTotNghiep.service.BillService;
import com.project.DuAnTotNghiep.utils.UserLoginUtil;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.http.HttpServletResponse;
import javax.swing.text.DateFormatter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ProductDetailRepository productDetailRepository;

    @Override
    public Page<BillDtoInterface> findAll(Pageable pageable) {
        return billRepository.listBill(pageable);
    }

    @Override
    public List<BillDtoInterface> findAll() {
        return billRepository.listBill();
    }

    @Override
    public Page<BillDtoInterface> searchListBill(String maDinhDanh,
                                                 LocalDateTime ngayTaoStart,
                                                 LocalDateTime ngayTaoEnd,
                                                 String trangThai,
                                                 String loaiDon,
                                                 String soDienThoai,
                                                 String hoVaTen,
                                                 Pageable pageable) {
        BillStatus status = null;
        InvoiceType invoiceType = null;

        try {
            status = BillStatus.valueOf(trangThai);
        } catch (IllegalArgumentException e) {

        }
        try {
            invoiceType = InvoiceType.valueOf(loaiDon);
        } catch (IllegalArgumentException e) {

        }
        return billRepository.listSearchBill( maDinhDanh,
                 ngayTaoStart,
                 ngayTaoEnd,
                status,
                 invoiceType,
                 soDienThoai,
                 hoVaTen,
                pageable);
    }

    @Override
    public List<BillDtoInterface> searchListBill(String maDinhDanh, LocalDateTime ngayTaoStart, LocalDateTime ngayTaoEnd, String trangThai, String loaiDon, String soDienThoai, String hoVaTen) {
        BillStatus status = null;
        InvoiceType invoiceType = null;

        try {
            status = BillStatus.valueOf(trangThai);
        } catch (IllegalArgumentException e) {

        }
        try {
            invoiceType = InvoiceType.valueOf(loaiDon);
        } catch (IllegalArgumentException e) {

        }
        return billRepository.listSearchBill( maDinhDanh,
                ngayTaoStart,
                ngayTaoEnd,
                status,
                invoiceType,
                soDienThoai,
                hoVaTen);
    }

    @Override
    public Bill updateStatus(String status, Long id) {

        // Nếu hủy thì cộng lại số lượng tồn
        if(status.equals("HUY")) {
            List<BillDetailProduct> billDetailProducts = billRepository.getBillDetailProduct(id);
            billDetailProducts.forEach(item -> {
                ProductDetail productDetail = productDetailRepository.findById(item.getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy thuộc tính " + item.getId()));
                int quantityBefore = productDetail.getQuantity();
                productDetail.setQuantity(quantityBefore + item.getSoLuong());
                productDetailRepository.save(productDetail);
            });
        }

        Bill bill = billRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy bill có mã" + id));
        bill.setStatus(BillStatus.valueOf(status));
        bill.setUpdateDate(LocalDateTime.now());
        return billRepository.save(bill);
    }

    @Override
    public BillDetailDtoInterface getBillDetail(Long maHoaDon) {
        return billRepository.getBillDetail(maHoaDon);
    }

    @Override
    public Page<Bill> getBillByStatus(String status, Pageable pageable) {
        Account account = UserLoginUtil.getCurrentLogin();
        BillStatus status1 = null;

        try {
            status1 = BillStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            // Handle the case where the input string does not match any enum constant
            // You can log an error, return a default value, or perform other error handling here.
        }
        return billRepository.findAllByStatusAndCustomer_Account_Id(status1, account.getId(), pageable);
    }

    @Override
    public List<BillDetailProduct> getBillDetailProduct(Long maHoaDon) {
        return billRepository.getBillDetailProduct(maHoaDon);
    }

    public void exportToExcel(HttpServletResponse response, Page<BillDtoInterface> bills, String exportUrl) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=bills.xlsx");

        // Tạo workbook Excel và các sheet, row, cell tương ứng
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Get the current date in the "dd-MM-yyyy" format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = dateFormat.format(new Date());

        // Create a sheet with the name "bill_dd-mm-yyyy"
        XSSFSheet sheet = workbook.createSheet("bill_" + currentDate);

        // Tạo tiêu đề cột
        XSSFRow headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Mã Hóa Đơn");
        headerRow.createCell(1).setCellValue("Họ và Tên");
        headerRow.createCell(2).setCellValue("Số điện thoại");
        headerRow.createCell(3).setCellValue("Ngày đặt");
        headerRow.createCell(4).setCellValue("Tổng tiền");
        headerRow.createCell(5).setCellValue("Trạng thái");
        headerRow.createCell(6).setCellValue("Loại đơn");
        headerRow.createCell(7).setCellValue("Hình thực thanh toán");

        int rowNum = 1;
        for (BillDtoInterface bill : bills) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(bill.getMaDinhDanh());
            row.createCell(1).setCellValue(bill.getHoVaTen());
            row.createCell(2).setCellValue(bill.getSoDienThoai());
            XSSFCell dateCell = row.createCell(3);
            XSSFCellStyle dateCellStyle = workbook.createCellStyle();
            XSSFDataFormat dataFormat = workbook.createDataFormat();
            dateCellStyle.setDataFormat(dataFormat.getFormat("dd/MM/yyyy"));
            dateCell.setCellStyle(dateCellStyle);
            dateCell.setCellValue(bill.getNgayTao());
            XSSFCell totalCell = row.createCell(4);
            totalCell.setCellValue(bill.getTongTien());

            XSSFCellStyle totalCellStyle = workbook.createCellStyle();
            XSSFDataFormat dataFormat1 = workbook.createDataFormat();
            totalCellStyle.setDataFormat(dataFormat1.getFormat("#,###"));
            totalCell.setCellStyle(totalCellStyle);
            String trangThaiText = "";
            switch (bill.getTrangThai()) {
                case CHO_XAC_NHAN:
                    trangThaiText = "Chờ xác nhận";
                    break;
                case CHO_LAY_HANG:
                    trangThaiText = "Đang xử lý";
                    break;
                case CHO_GIAO_HANG:
                    trangThaiText = "Chờ giao hàng";
                    break;
                case HOAN_THANH:
                    trangThaiText = "Hoàn thành";
                    break;
                case HUY:
                    trangThaiText = "Hủy";
                    break;
                default:
                    trangThaiText = "  ";
            }
            String loaiDonText = "";
            switch (bill.getLoaiDon()) {
                case OFFLINE:
                    loaiDonText = "Tại quầy";
                    break;
                case ONLINE:
                    loaiDonText = "Trực tuyến";
                    break;
                default:
                    loaiDonText = "  ";
            }
            row.createCell(5).setCellValue(trangThaiText);
            row.createCell(6).setCellValue(loaiDonText);
            row.createCell(7).setCellValue(bill.getHinhThucThanhToan());

            XSSFCell linkCell = row.createCell(8);
            XSSFRichTextString linkText = new XSSFRichTextString(" ");
            CreationHelper createHelper = workbook.getCreationHelper();
            XSSFHyperlink hyperlink = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress(exportUrl);
            linkCell.setHyperlink(hyperlink);
            linkCell.setCellValue(linkText);
        }

        try (OutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
            outputStream.flush();
        }
    }

    @Override
    public String exportPdf(HttpServletResponse response, Long maHoaDon) throws DocumentException, IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/pdf");

        ITextRenderer renderer = new ITextRenderer();
        renderer.getFontResolver().addFont("/static/fonts/time-new-roman/SVN-Times New Roman.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        String htmlContent = getHtmlContent(maHoaDon);
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        try (OutputStream outputStream = response.getOutputStream()) {
            renderer.createPDF(outputStream, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHtmlContent(Long maHoaDon) {
        BillDetailDtoInterface billDetailDtoInterface = billRepository.getBillDetail(maHoaDon);
        List<BillDetailProduct> billDetailProduct = billRepository.getBillDetailProduct(maHoaDon);
        String email = billDetailDtoInterface.getEmail();
        if (email == null) {
            email = "";
        }
        String address = billDetailDtoInterface.getDiaChi();
        if (address == null) {
            address = "";
        }

        String customerName = billDetailDtoInterface.getTenKhachHang();
        if (customerName == null) {
            customerName = "";
        }

        String customerPhone = billDetailDtoInterface.getSoDienThoai();
        if (customerPhone == null) {
            customerPhone = "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String htmlContent = "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></meta>\n" +
                "    <title>Hóa đơn bán hàng</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: SVN-Times New Roman;\">\n" +
                "<h1 style=\"text-align: center\">HÓA ĐƠN BÁN HÀNG</h1>\n" +
                "<h3 style=\"text-align: center\"> Tòa nhà FPT Polytechnic, Cổng số 2, 13 P. Trịnh Văn Bô</h3>\n" +
                "<h3 style=\"text-align: center\">Xuân Phương, Nam Từ Liêm, Hà Nội</h3>\n\n" +
                "<h5> Mã hóa đơn: " + billDetailDtoInterface.getMaDinhDanh() + "</h5>\n" +
                "<h5> Họ và tên: " + customerName + "</h5>\n" +
                "<h5> Số điện thoại :" + customerPhone + "</h5>\n" +
                "<h5> Email: " + email + "</h5>\n" +
                "<h5> Địa chỉ:" + address + "</h5>\n" +
                "<h5> Ngày thanh toán: " + billDetailDtoInterface.getCreatedDate().format(formatter) + "</h5>\n" +
                "<h3>Danh sách sản phẩm:</h3>\n" +
                "<table border=\"1\" style=\"border-collapse: collapse;\">\n" +
                "<tr>\n" +
                "<th>Tên sản phẩm</th>\n" +
                "<th>Màu sắc</th>\n" +
                "<th>Kích cỡ</th>\n" +
                "<th>Giá tiền</th>\n" +
                "<th>Số lượng</th>\n" +
                "<th>Tổng tiền</th>\n" +
                "</tr>\n";
        Double totalMoney = Double.valueOf(0);
        for (BillDetailProduct item:
                billDetailProduct) {
            totalMoney += item.getGiaTien() * item.getSoLuong();
        }
        Locale locale = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        for (int i = 0; i < billDetailProduct.size(); i++) {
            String productName = billDetailProduct.get(i).getTenSanPham();
            String color = billDetailProduct.get(i).getTenMau();
            String size = billDetailProduct.get(i).getKichCo();
            String price = currencyFormatter.format(billDetailProduct.get(i).getGiaTien());
            String quantity = String.valueOf(billDetailProduct.get(i).getSoLuong());
            String total = currencyFormatter.format(billDetailProduct.get(i).getTongTien());

            htmlContent += "<tr>\n" +
                    "<td>" + productName + "</td>\n" +
                    "<td>" + color + "</td>\n" +
                    "<td>" + size + "</td>\n" +
                    "<td>" + price + "</td>\n" +
                    "<td>" + quantity + "</td>\n" +
                    "<td>" + total + "</td>\n" +
                    "</tr>\n";
        }
        htmlContent += "</table>\n" +
                "<h5>Tổng tiền: " + currencyFormatter.format(totalMoney) + "</h5>\n" +
                "<h5>Tiền giảm giá: " + currencyFormatter.format(billDetailDtoInterface.getTienKhuyenMai()) + "</h5>\n" +
                "<h4>Tổng tiền thanh toán: " + currencyFormatter.format(totalMoney - billDetailDtoInterface.getTienKhuyenMai()) + "</h4>\n" +
                "</body>\n" +
                "</html>";
        return htmlContent;
    }

    @Override
    public Page<Bill> getBillByAccount(Pageable pageable) {
        Account account = UserLoginUtil.getCurrentLogin();
        return billRepository.findByCustomer_Account_Id(account.getId(), pageable);
    }

    @Override
    public void deleteById(Long id) {
        billRepository.deleteById(id);
    }

    @Override
    public Page<BillDto> searchBillJson(SearchBillDto searchBillDto, Pageable pageable) {
        Specification<Bill> spec = new BillSpecification(searchBillDto);
        Page<Bill> bills = billRepository.findAll(spec, pageable);
        return bills.map(this::convertToDto);
    }

    @Override
    public Page<BillDto> getAllValidBillToReturn(Pageable pageable) {
        return billRepository.findValidBillToReturn(pageable).map(this::convertToDto);
    }

    private BillDto convertToDto(Bill bill) {
        BillDto billDto = new BillDto();
        billDto.setId(bill.getId());
        billDto.setCode(bill.getCode());
        billDto.setCreateDate(bill.getCreateDate());
        billDto.setStatus(bill.getStatus());
        billDto.setUpdateDate(bill.getUpdateDate());
        CustomerDto customer = new CustomerDto();
        if(bill.getCustomer() != null) {
            customer.setName(bill.getCustomer().getName());
            customer.setId(bill.getCustomer().getId());
            customer.setCode(bill.getCustomer().getCode());
            customer.setCode(bill.getCustomer().getCode());
        }
        billDto.setCustomer(customer);
        Double total = Double.valueOf(0);
        for (BillDetail billDetail:
             bill.getBillDetail()) {
            total += billDetail.getQuantity() * billDetail.getMomentPrice();
        }
        billDto.setTotalAmount(total);
        return billDto;
    }

}
