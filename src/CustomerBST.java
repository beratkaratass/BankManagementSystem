class Node {
    Customer customer;
    Node left, right;

    public Node(Customer customer) {
        this.customer = customer;
        this.left = null;
        this.right = null;
    }
}

public class CustomerBST {
    private Node root;
    private int size;

    public CustomerBST() {
        this.root = null;
        this.size = 0;
    }

    public boolean insert(Customer customer) {
        int beforeSize = size;
        root = insertRec(root, customer);
        return size > beforeSize;
    }

    private Node insertRec(Node node, Customer customer) {
        if (node == null) {
            size++;
            return new Node(customer);
        }
        int cmp = customer.getId().compareTo(node.customer.getId());
        if (cmp < 0) {
            node.left = insertRec(node.left, customer);
        } else if (cmp > 0) {
            node.right = insertRec(node.right, customer);
        }
        return node;
    }

    public Customer search(String id) {
        Node result = searchRec(root, id);
        return result == null ? null : result.customer;
    }

    private Node searchRec(Node node, String id) {
        if (node == null) return null;
        int cmp = id.compareTo(node.customer.getId());
        if (cmp == 0) return node;
        if (cmp < 0) return searchRec(node.left, id);
        return searchRec(node.right, id);
    }

    public int getSize() {
        return size;
    }
}
