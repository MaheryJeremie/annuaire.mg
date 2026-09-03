package mg.annuaire.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mg.annuaire.app.data.model.Avis
import mg.annuaire.app.data.model.Commune
import mg.annuaire.app.data.model.Metier
import mg.annuaire.app.data.model.Prestataire
import mg.annuaire.app.data.model.PrestataireQuartier
import mg.annuaire.app.data.model.Quartier
import mg.annuaire.app.data.model.Tarif
import mg.annuaire.app.data.model.User

@Database(
    entities = [
        User::class,
        Metier::class,
        Commune::class,
        Quartier::class,
        Prestataire::class,
        PrestataireQuartier::class,
        Tarif::class,
        Avis::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AnnuaireDatabase : RoomDatabase() {
    abstract fun dao(): AnnuaireDao

    companion object {
        @Volatile
        private var INSTANCE: AnnuaireDatabase? = null

        fun getInstance(context: Context): AnnuaireDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AnnuaireDatabase::class.java,
                    "annuaire_mg.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
