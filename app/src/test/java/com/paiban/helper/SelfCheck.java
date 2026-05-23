package com.paiban.helper;

import com.paiban.helper.data.db.HistoryEntity;
import com.paiban.helper.data.repository.EditorRepository;
import com.paiban.helper.domain.analysis.ContentClassifier;
import com.paiban.helper.domain.model.ContentType;
import com.paiban.helper.domain.render.HtmlSanitizer;
import com.paiban.helper.domain.render.MarkdownConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class SelfCheck {
    public static void main(String[] args) {
        assertEquals(ContentType.Markdown, new ContentClassifier().classify("# 标题\n\n- 列表项"), "classify markdown");

        String markdownHtml = new MarkdownConverter().convert("# 标题\n\n**加粗**");
        assertTrue(markdownHtml.contains("<h1>标题</h1>"), "markdown heading");
        assertTrue(markdownHtml.contains("<strong>加粗</strong>"), "markdown strong");

        String sanitized = new HtmlSanitizer().sanitize("<p onclick='x()'>hi</p><script>alert(1)</script>");
        assertFalse(sanitized.contains("script"), "sanitize script");
        assertFalse(sanitized.contains("onclick"), "sanitize onclick");
        assertTrue(sanitized.contains("<p>hi</p>"), "sanitize keep p");

        List<HistoryEntity> items = Arrays.asList(
                new HistoryEntity(1, "A", "a", "", "Markdown", "minimalist-0", false, 1, 1),
                new HistoryEntity(2, "B", "b", "", "Markdown", "minimalist-0", true, 2, 2)
        );
        List<HistoryEntity> trimmed = EditorRepository.trimHistory(items, 1);
        List<Long> ids = trimmed.stream().map(HistoryEntity::getId).collect(Collectors.toList());
        assertEquals(Arrays.asList(2L), ids, "trim history favorites");

        System.out.println("SELF_CHECK_OK");
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }

    private static void assertFalse(boolean condition, String label) {
        if (condition) throw new AssertionError(label);
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(label + " expected=" + expected + " actual=" + actual);
        }
    }
}
