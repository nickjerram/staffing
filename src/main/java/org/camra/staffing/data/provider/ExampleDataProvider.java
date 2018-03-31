package org.camra.staffing.data.provider;

import com.vaadin.data.provider.*;
import com.vaadin.shared.Registration;
import org.camra.staffing.data.service.AbstractExampleService;
import org.springframework.data.domain.Example;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class ExampleDataProvider <DTO,ENTITY> implements ConfigurableFilterDataProvider<DTO, Example<ENTITY>, Map<String,String>> {

    protected ConfigurableFilterDataProvider<DTO, Example<ENTITY>, Example<ENTITY>> delegate;
    private Map<String,String> filters = new HashMap<>();

    protected void createDelegate() {
        CallbackDataProvider<DTO,Example<ENTITY>> callbackDataProvider = DataProvider.fromFilteringCallbacks(
                q -> getService().getRecords(q).stream(),
                q -> getService().countRecords(q)
        );
        delegate = callbackDataProvider.withConfigurableFilter((q,c) -> c);
    }

    protected abstract AbstractExampleService<DTO,ENTITY> getService();

    @Override
    public boolean isInMemory() {
        return delegate.isInMemory();
    }

    @Override
    public int size(Query<DTO, Example<ENTITY>> query) {
        return delegate.size(query);
    }

    @Override
    public Stream<DTO> fetch(Query<DTO, Example<ENTITY>> query) {
        return delegate.fetch(query);
    }

    @Override
    public void refreshItem(DTO volunteerDTO) {
        delegate.refreshItem(volunteerDTO);
    }

    @Override
    public void refreshAll() {
        delegate.refreshAll();
    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<DTO> dataProviderListener) {
        return delegate.addDataProviderListener(dataProviderListener);
    }
}
