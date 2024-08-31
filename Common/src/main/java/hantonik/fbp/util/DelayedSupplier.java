package hantonik.fbp.util;

import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@Setter(onParam_ = { @Nonnull })
public class DelayedSupplier<T> implements Supplier<T> {
    private Supplier<T> supplier;

    @Override
    public T get() {
        if (this.supplier == null)
            throw new IllegalStateException("Attempted to call DelayedSupplier::get() before the supplier was set.");

        return this.supplier.get();
    }
}
