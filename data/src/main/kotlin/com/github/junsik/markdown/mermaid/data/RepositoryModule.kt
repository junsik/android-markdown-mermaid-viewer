package com.github.junsik.markdown.mermaid.data

import android.content.Context
import com.github.junsik.markdown.mermaid.data.repository.DocumentRepositoryImpl
import com.github.junsik.markdown.mermaid.domain.repository.DocumentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDocumentRepository(
        @ApplicationContext context: Context
    ): DocumentRepository {
        return DocumentRepositoryImpl(context)
    }
}
