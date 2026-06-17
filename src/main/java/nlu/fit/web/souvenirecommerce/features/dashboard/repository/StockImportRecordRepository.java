package nlu.fit.web.souvenirecommerce.features.dashboard.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.StockImportSummaryDTO;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.StockImportRecord;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class StockImportRecordRepository extends AbsBaseRepository<Long, StockImportRecord> {

    public StockImportRecordRepository() {
        super(StockImportRecord.class);
    }

    public List<StockImportRecord> findRecentImports(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        return getSession()
                .createQuery("""
                        select r
                        from StockImportRecord r
                        order by r.createdAt desc, r.id desc
                        """, StockImportRecord.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public StockImportSummaryDTO loadMonthlySummary(LocalDateTime startOfMonth, int lowStockThreshold) {
        Object[] row = getSession()
                .createQuery("""
                        select count(r), sum(r.quantity), sum(r.totalCost), max(r.createdAt)
                        from StockImportRecord r
                        where r.createdAt >= :startOfMonth
                        """, Object[].class)
                .setParameter("startOfMonth", toDate(startOfMonth))
                .uniqueResult();

        long importCount = 0L;
        long totalQuantity = 0L;
        BigDecimal totalCost = BigDecimal.ZERO;
        Date latestImportAt = null;

        if (row != null) {
            if (row[0] != null) {
                importCount = ((Number) row[0]).longValue();
            }
            if (row[1] != null) {
                totalQuantity = ((Number) row[1]).longValue();
            }
            if (row[2] != null) {
                totalCost = toBigDecimal(row[2]);
            }
            if (row[3] != null) {
                latestImportAt = (Date) row[3];
            }
        }

        long lowStockCount = countLowStockProducts(lowStockThreshold);

        return StockImportSummaryDTO.builder()
                .importCount(importCount)
                .totalQuantity(totalQuantity)
                .totalCost(totalCost)
                .lowStockCount(lowStockCount)
                .latestImportAt(latestImportAt)
                .build();
    }

    public StockImportRecord importStock(Long productId,
                                         int quantity,
                                         BigDecimal unitCost,
                                         String note,
                                         Long importedByUserId,
                                         String importedByName) {
        if (productId == null) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần nhập");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }
        if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá nhập không hợp lệ");
        }
        if (importedByUserId == null) {
            throw new IllegalArgumentException("Không xác định được người thao tác");
        }

        Session session = getSession();
        Transaction transaction = session.getTransaction();

        if (transaction == null || !transaction.isActive()) {
            transaction = session.beginTransaction();
        }

        try {
            Product product = session.find(Product.class, productId);
            if (product == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xóa");
            }

            int stockBefore = product.getStockQuantity();
            int stockAfter = stockBefore + quantity;
            BigDecimal normalizedUnitCost = unitCost.setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalCost = normalizedUnitCost.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);

            product.setStockQuantity(stockAfter);

            StockImportRecord record = StockImportRecord.builder()
                    .productId(product.getId())
                    .productNameSnapshot(product.getName())
                    .quantity(quantity)
                    .unitCost(normalizedUnitCost)
                    .totalCost(totalCost)
                    .stockBefore(stockBefore)
                    .stockAfter(stockAfter)
                    .note(normalizeNote(note))
                    .importedByUserId(importedByUserId)
                    .importedByName(normalizeImportedByName(importedByName, importedByUserId))
                    .build();

            session.persist(record);
            session.flush();

            if (transaction.isActive()) {
                transaction.commit();
            }

            return record;
        } catch (RuntimeException ex) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw ex;
        }
    }

    public long countLowStockProducts(int threshold) {
        Long count = getSession()
                .createQuery("""
                        select count(p)
                        from Product p
                        where p.stockQuantity <= :threshold
                        """, Long.class)
                .setParameter("threshold", threshold)
                .uniqueResult();
        return count == null ? 0L : count;
    }

    private Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return BigDecimal.ZERO;
    }

    private String normalizeNote(String note) {
        if (note == null) {
            return null;
        }
        String trimmed = note.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeImportedByName(String importedByName, Long importedByUserId) {
        if (importedByName != null && !importedByName.isBlank()) {
            return importedByName.trim();
        }
        return "User #" + importedByUserId;
    }
}
