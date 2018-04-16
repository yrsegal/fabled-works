package wiresegal.fabled.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author WireSegal
 * Created at 6:14 PM on 4/10/18.
 */
@IFMLLoadingPlugin.Name("FabledWorks Plugin")
@IFMLLoadingPlugin.TransformerExclusions("wiresegal.fabled.asm")
@IFMLLoadingPlugin.SortingIndex(1001)
public class FabledCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                "wiresegal.fabled.asm.FabledAsmTransformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // NO-OP
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
