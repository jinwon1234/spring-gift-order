package gift;

import java.util.List;

public class PageResponse<T> {

    private List<T> content;
    private PageInfo page;

    protected PageResponse() {}

    public static class PageInfo {
        private int size;
        private int number;
        private int totalElements;
        private int totalPages;

        protected PageInfo() {
        }

        public int getSize() {
            return size;
        }

        public int getNumber() {
            return number;
        }

        public int getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }

    public List<T> getContent() {
        return content;
    }

    public PageInfo getPage() {
        return page;
    }

}

