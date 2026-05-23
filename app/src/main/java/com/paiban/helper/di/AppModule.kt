package com.paiban.helper.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.paiban.helper.BuildConfig
import com.paiban.helper.data.db.AppDatabase
import com.paiban.helper.data.db.AiConfigDao
import com.paiban.helper.data.repository.AiSettingsRepository
import com.paiban.helper.data.repository.EditorRepository
import com.paiban.helper.data.repository.SettingsRepository
import com.paiban.helper.domain.analysis.ContentClassifier
import com.paiban.helper.domain.ai.AiChatRepository
import com.paiban.helper.domain.ai.DeepSeekClient
import com.paiban.helper.domain.ai.DeepSeekConfig
import com.paiban.helper.domain.ai.AiSecretCrypto
import com.paiban.helper.domain.clipboard.ClipboardInspector
import com.paiban.helper.domain.files.ImportExportManager
import com.paiban.helper.domain.render.HtmlSanitizer
import com.paiban.helper.domain.render.InlineArticleRenderer
import com.paiban.helper.domain.render.MarkdownConverter
import com.paiban.helper.domain.render.PreviewDocumentBuilder
import com.paiban.helper.domain.template.ArticleTemplateRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "paiban.db")
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideDraftDao(database: AppDatabase) = database.draftDao()

    @Provides
    fun provideHistoryDao(database: AppDatabase) = database.historyDao()

    @Provides
    fun provideAiConfigDao(database: AppDatabase): AiConfigDao = database.aiConfigDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return SettingsRepository.createDataStore(context)
    }

    @Provides
    @Singleton
    fun provideEditorRepository(
        draftDao: com.paiban.helper.data.db.DraftDao,
        historyDao: com.paiban.helper.data.db.HistoryDao,
    ): EditorRepository {
        return EditorRepository(draftDao, historyDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences>,
    ): SettingsRepository {
        return SettingsRepository(dataStore)
    }

    @Provides
    @Singleton
    fun provideAiSecretCrypto(): AiSecretCrypto = AiSecretCrypto()

    @Provides
    @Singleton
    fun provideAiSettingsRepository(
        aiConfigDao: AiConfigDao,
        secretCrypto: AiSecretCrypto,
    ): AiSettingsRepository {
        return AiSettingsRepository(
            dao = aiConfigDao,
            crypto = secretCrypto,
            builtInApiKey = BuildConfig.DEEPSEEK_API_KEY,
        )
    }

    @Provides
    @Singleton
    fun provideDeepSeekConfig(): DeepSeekConfig {
        return DeepSeekConfig(
            apiKeyValue = BuildConfig.DEEPSEEK_API_KEY,
            model = DeepSeekConfig.DEFAULT_MODEL,
        )
    }

    @Provides
    @Singleton
    fun provideDeepSeekClient(
        config: DeepSeekConfig,
    ): DeepSeekClient = DeepSeekClient(config)

    @Provides
    @Singleton
    fun provideAiChatRepository(
        @ApplicationContext context: Context,
        client: DeepSeekClient,
    ): AiChatRepository {
        return AiChatRepository(
            storageDir = File(context.filesDir, "ai-chat"),
            client = client,
        )
    }

    @Provides
    @Singleton
    fun provideClipboardInspector(): ClipboardInspector = ClipboardInspector()

    @Provides
    @Singleton
    fun provideTemplateRepository(@ApplicationContext context: Context): ArticleTemplateRepository {
        return ArticleTemplateRepository {
            context.assets.open("templates/templates.json").bufferedReader().use { it.readText() }
        }
    }

    @Provides
    @Singleton
    fun provideContentClassifier(): ContentClassifier = ContentClassifier()

    @Provides
    @Singleton
    fun provideMarkdownConverter(): MarkdownConverter = MarkdownConverter()

    @Provides
    @Singleton
    fun provideHtmlSanitizer(): HtmlSanitizer = HtmlSanitizer()

    @Provides
    @Singleton
    fun provideInlineArticleRenderer(): InlineArticleRenderer = InlineArticleRenderer()

    @Provides
    @Singleton
    fun providePreviewDocumentBuilder(
        classifier: ContentClassifier,
        markdownConverter: MarkdownConverter,
        sanitizer: HtmlSanitizer,
        templateRepository: ArticleTemplateRepository,
        inlineArticleRenderer: InlineArticleRenderer,
    ): PreviewDocumentBuilder {
        return PreviewDocumentBuilder(
            classifier = classifier,
            markdownConverter = markdownConverter,
            sanitizer = sanitizer,
            templateRepository = templateRepository,
            inlineArticleRenderer = inlineArticleRenderer,
        )
    }

    @Provides
    @Singleton
    fun provideImportExportManager(): ImportExportManager = ImportExportManager()
}
