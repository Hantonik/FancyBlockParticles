package hantonik.fbp.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import hantonik.fbp.screen.FBPOptionsScreen;

public class FBPModMenuApi implements ModMenuApi {
    @Override
    public ConfigScreenFactory<FBPOptionsScreen> getModConfigScreenFactory() {
        return FBPOptionsScreen::new;
    }
}
