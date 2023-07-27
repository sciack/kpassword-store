package passwordStore.tags

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

internal val tagModule = DI.Module(name = "tagModule") {
    bind<TagRepository> {
        singleton {
            TagRepository(instance())
        }
    }
}