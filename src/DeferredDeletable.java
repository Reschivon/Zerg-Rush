/**
 * Objects implementing the deletable interface have multiple
 * unsynchronized iterators, which means deletion must be deferred
 * to the right time
 */
public class DeferredDeletable {
    private boolean delete = false;

    boolean deleteRequested() {
        return delete;
    }

    protected void markDeletable() {
        delete = true;
    }
}
