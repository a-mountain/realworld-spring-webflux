package helpers.article;

public class FindArticlesRequest {
    private int limit = 0;
    private int offset = 20;
    private String author = null;
    private String tag = null;
    private String favorited = null;

    public int getLimit() {
        return limit;
    }

    public FindArticlesRequest setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public int getOffset() {
        return offset;
    }

    public FindArticlesRequest setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public FindArticlesRequest setAuthor(String author) {
        this.author = author;
        return this;

    }

    public String getTag() {
        return tag;
    }

    public FindArticlesRequest setTag(String tag) {
        this.tag = tag;
        return this;

    }

    public String getFavorited() {
        return favorited;
    }

    public FindArticlesRequest setFavorited(String favorited) {
        this.favorited = favorited;
        return this;
    }
}
