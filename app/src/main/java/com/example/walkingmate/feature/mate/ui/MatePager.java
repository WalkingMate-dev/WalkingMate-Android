package com.example.walkingmate.feature.mate.ui;

import java.util.ArrayList;
import java.util.HashMap;

final class MatePager {
    private final int pageSize;

    MatePager(int pageSize) {
        this.pageSize = pageSize;
    }

    ArrayList<String> filterMateDocIds(
            ArrayList<String> mateDocIds,
            String keyword,
            int searchMode,
            HashMap<String, String> writers,
            HashMap<String, String> titles
    ) {
        ArrayList<String> result = new ArrayList<>();
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            result.addAll(mateDocIds);
            return result;
        }

        String lowerKeyword = normalizedKeyword.toLowerCase();
        for (String docId : mateDocIds) {
            String source = searchMode == 0 ? writers.get(docId) : titles.get(docId);
            if (source != null && source.toLowerCase().contains(lowerKeyword)) {
                result.add(docId);
            }
        }
        return result;
    }

    int pageCount(int totalItems) {
        if (totalItems <= 0) {
            return 1;
        }
        return (totalItems + pageSize - 1) / pageSize;
    }

    int clampPage(int currentPage, int totalPages) {
        if (currentPage < 0) {
            return 0;
        }
        if (currentPage > totalPages - 1) {
            return totalPages - 1;
        }
        return currentPage;
    }

    int pageItemCount(int totalItems, int currentPage) {
        if (totalItems <= 0) {
            return 0;
        }
        int start = currentPage * pageSize;
        return Math.min(pageSize, totalItems - start);
    }

    int toRealPosition(int page, int positionInPage) {
        return page * pageSize + positionInPage;
    }
}
