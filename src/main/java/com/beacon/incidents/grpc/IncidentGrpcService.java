package com.beacon.incidents.grpc;

import com.beacon.incidents.domain.TimelineEntry;
import com.beacon.incidents.service.IncidentPage;
import com.beacon.incidents.service.IncidentService;
import com.beacon.incidents.service.IncidentView;
import com.beacon.incidents.v1.AddTimelineEntryRequest;
import com.beacon.incidents.v1.AddTimelineEntryResponse;
import com.beacon.incidents.v1.CreateIncidentRequest;
import com.beacon.incidents.v1.CreateIncidentResponse;
import com.beacon.incidents.v1.GetIncidentRequest;
import com.beacon.incidents.v1.GetIncidentResponse;
import com.beacon.incidents.v1.IncidentServiceGrpc;
import com.beacon.incidents.v1.ListIncidentsRequest;
import com.beacon.incidents.v1.ListIncidentsResponse;
import com.beacon.incidents.v1.UpdateStatusRequest;
import com.beacon.incidents.v1.UpdateStatusResponse;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IncidentGrpcService extends IncidentServiceGrpc.IncidentServiceImplBase {

    private final IncidentService service;

    public IncidentGrpcService(IncidentService service) {
        this.service = service;
    }

    @Override
    public void createIncident(CreateIncidentRequest request, StreamObserver<CreateIncidentResponse> observer) {
        GrpcCalls.handle(observer, () -> {
            IncidentView view = service.create(
                    request.getTitle(),
                    request.getService(),
                    ProtoMapper.severityRequired(request.getSeverity()),
                    request.getActor());
            return CreateIncidentResponse.newBuilder()
                    .setIncident(ProtoMapper.incidentToProto(view.incident(), view.timeline()))
                    .build();
        });
    }

    @Override
    public void getIncident(GetIncidentRequest request, StreamObserver<GetIncidentResponse> observer) {
        GrpcCalls.handle(observer, () -> {
            IncidentView view = service.get(UUID.fromString(request.getId()));
            return GetIncidentResponse.newBuilder()
                    .setIncident(ProtoMapper.incidentToProto(view.incident(), view.timeline()))
                    .build();
        });
    }

    @Override
    public void listIncidents(ListIncidentsRequest request, StreamObserver<ListIncidentsResponse> observer) {
        GrpcCalls.handle(observer, () -> {
            int pageIndex = parsePageToken(request.getPageToken());
            String serviceFilter = request.getService().isBlank() ? null : request.getService();
            IncidentPage page = service.list(
                    ProtoMapper.statusOrNull(request.getStatus()),
                    ProtoMapper.severityOrNull(request.getSeverity()),
                    serviceFilter,
                    request.getPageSize(),
                    pageIndex);

            ListIncidentsResponse.Builder response = ListIncidentsResponse.newBuilder();
            // En el listado el timeline va vacío por eficiencia.
            page.items().forEach(i -> response.addIncidents(ProtoMapper.incidentToProto(i, List.of())));
            if (page.hasNext()) {
                response.setNextPageToken(String.valueOf(pageIndex + 1));
            }
            return response.build();
        });
    }

    @Override
    public void updateStatus(UpdateStatusRequest request, StreamObserver<UpdateStatusResponse> observer) {
        GrpcCalls.handle(observer, () -> {
            IncidentView view = service.updateStatus(
                    UUID.fromString(request.getId()),
                    ProtoMapper.statusRequired(request.getStatus()),
                    request.getActor());
            return UpdateStatusResponse.newBuilder()
                    .setIncident(ProtoMapper.incidentToProto(view.incident(), view.timeline()))
                    .build();
        });
    }

    @Override
    public void addTimelineEntry(AddTimelineEntryRequest request,
                                 StreamObserver<AddTimelineEntryResponse> observer) {
        GrpcCalls.handle(observer, () -> {
            TimelineEntry entry = service.addTimelineEntry(
                    UUID.fromString(request.getIncidentId()),
                    ProtoMapper.kindRequired(request.getKind()),
                    request.getMessage(),
                    request.getActor());
            return AddTimelineEntryResponse.newBuilder()
                    .setEntry(ProtoMapper.entryToProto(entry))
                    .build();
        });
    }

    private static int parsePageToken(String token) {
        if (token == null || token.isBlank()) {
            return 0;
        }
        int index = Integer.parseInt(token); // NumberFormatException -> INVALID_ARGUMENT
        if (index < 0) {
            throw new IllegalArgumentException("invalid page_token");
        }
        return index;
    }
}
