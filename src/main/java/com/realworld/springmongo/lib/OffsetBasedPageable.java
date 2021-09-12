package com.realworld.springmongo.lib;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetBasedPageable implements Pageable {
    private final int limit;
    private final int offset;
    private final Sort sort;

    private OffsetBasedPageable(int limit, int offset, Sort sort) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must not be less than one");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset index must not be less than zero");
        }
        this.limit = limit;
        this.offset = offset;
        this.sort = sort;
    }

    public static Pageable of(int limit, int offset) {
        return new OffsetBasedPageable(limit, offset, Sort.unsorted());
    }

    public static Pageable of(int limit, int offset, Sort sort) {
        return new OffsetBasedPageable(limit, offset, sort);
    }

    @Override
    public int getPageNumber() {
        throw unsupportedOperation();
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        throw unsupportedOperation();
    }

    @Override
    public Pageable previousOrFirst() {
        throw unsupportedOperation();
    }

    @Override
    public Pageable first() {
        throw unsupportedOperation();
    }

    @Override
    public Pageable withPage(int pageNumber) {
        throw unsupportedOperation();
    }

    @Override
    public boolean hasPrevious() {
        throw unsupportedOperation();
    }

    private UnsupportedOperationException unsupportedOperation() {
        return new UnsupportedOperationException("OffsetBasedPageable has no pages. Contains only offset and page size");
    }
}
