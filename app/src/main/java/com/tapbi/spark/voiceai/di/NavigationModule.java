package com.tapbi.spark.voiceai.di;

import com.tapbi.spark.voiceai.navigation.MainNavigator;
import com.tapbi.spark.voiceai.navigation.MainNavigatorImpl;
import com.tapbi.spark.voiceai.navigation.utils.InternalNavigation;
import com.tapbi.spark.voiceai.navigation.utils.RouteNavigation;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public interface NavigationModule {
    @Binds
    @Singleton
    MainNavigator bindMainNavigator(MainNavigatorImpl mainNavigator);

    @Binds
    @Singleton
    RouteNavigation bindRouteNavigation(InternalNavigation internalNavigation);
}
