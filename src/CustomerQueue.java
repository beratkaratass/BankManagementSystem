import java.util.ArrayList;
import java.util.List;

class QueueNode {
    Customer customer;
    QueueNode next;

    public QueueNode(Customer customer) {
        this.customer = customer;
        this.next = null;
    }
}

public class CustomerQueue {
    private QueueNode front;
    private QueueNode rear;
    private int size;

    public CustomerQueue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    public void enqueue(Customer customer) {
        QueueNode newNode = new QueueNode(customer);
        if (rear == null) {
            front = rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    public Customer dequeue() {
        if (front == null) return null;
        Customer c = front.customer;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return c;
    }

    public Customer peek() {
        return front == null ? null : front.customer;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int getSize() {
        return size;
    }

    public List<Customer> snapshot() {
        List<Customer> list = new ArrayList<>();
        QueueNode cur = front;
        while (cur != null) {
            list.add(cur.customer);
            cur = cur.next;
        }
        return list;
    }

    public boolean contains(String id) {
        QueueNode cur = front;
        while (cur != null) {
            if (cur.customer.getId().equals(id)) return true;
            cur = cur.next;
        }
        return false;
    }
}
