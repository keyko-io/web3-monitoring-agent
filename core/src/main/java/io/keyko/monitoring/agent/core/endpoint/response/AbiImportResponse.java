package io.keyko.monitoring.agent.core.endpoint.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbiImportResponse {

    List<AddEventFilterResponse> listEventFilters= new ArrayList<>();
    List<AddViewFilterResponse> listViewFilters= new ArrayList<>();

}
