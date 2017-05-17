package cn.whaley.materialngaclient;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by whaley on 2017/5/16.
 */
@Singleton
@Component(modules = {ExternalApplicationModule.class})
public interface ApplicationComponent extends ApplicationGraph {
}
