package fi.nottingham.mobilefood;

import dagger.Module;
import fi.nottingham.mobilefood.ui.MainActivity;

@Module(
    injects = MainActivity.class,
    complete = false,
    library = true
)
public class MobilefoodModule {
  // TODO put your application-specific providers here!
}
