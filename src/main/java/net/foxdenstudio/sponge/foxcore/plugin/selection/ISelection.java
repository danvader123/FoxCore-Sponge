package net.foxdenstudio.sponge.foxcore.plugin.selection;

import com.flowpowered.math.vector.Vector3i;
import net.foxdenstudio.sponge.foxcore.plugin.util.BoundingBox3;
import net.foxdenstudio.sponge.foxcore.plugin.util.IWorldBounded;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public interface ISelection extends Iterable<Vector3i>, IWorldBounded {

    Text overview();

    Optional<Text> details();

    String type();

    /**
     * @return The number of blocks selected.
     */
    int size();

    Optional<BoundingBox3> bounds();

    default boolean isEmpty(){
        return size() == 0;
    }
}