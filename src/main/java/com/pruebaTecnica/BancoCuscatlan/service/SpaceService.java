package com.pruebaTecnica.BancoCuscatlan.service;

import com.pruebaTecnica.BancoCuscatlan.domain.entity.Space;
import com.pruebaTecnica.BancoCuscatlan.dto.CreateSpaceRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.SpaceResponse;
import com.pruebaTecnica.BancoCuscatlan.exception.ResourceNotFoundException;
import com.pruebaTecnica.BancoCuscatlan.mapper.SpaceMapper;
import com.pruebaTecnica.BancoCuscatlan.repository.SpaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final SpaceMapper spaceMapper;

    public SpaceService(SpaceRepository spaceRepository, SpaceMapper spaceMapper) {
        this.spaceRepository = spaceRepository;
        this.spaceMapper = spaceMapper;
    }

    @Transactional
    public SpaceResponse createSpace(CreateSpaceRequest request) {
        Space space = spaceMapper.toEntity(request);
        if (space.getActive() == null) {
            space.setActive(true);
        }
        Space created = spaceRepository.save(space);
        return spaceMapper.toResponse(created);
    }

    @Transactional(readOnly = true)
    public SpaceResponse getSpaceById(Long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con id: " + id));
        return spaceMapper.toResponse(space);
    }

    @Transactional(readOnly = true)
    public List<SpaceResponse> getAllSpaces() {
        return spaceRepository.findAll().stream()
                .map(spaceMapper::toResponse)
                .toList();
    }

    @Transactional
    public SpaceResponse updateSpace(Long id, CreateSpaceRequest request) {
        Space existing = spaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con id: " + id));

        existing.setName(request.getName());
        existing.setType(request.getType());
        existing.setCapacity(request.getCapacity());
        existing.setLocation(request.getLocation());
        existing.setHourlyRate(request.getHourlyRate());
        existing.setActive(request.getActive() != null ? request.getActive() : existing.getActive());

        Space updated = spaceRepository.save(existing);
        return spaceMapper.toResponse(updated);
    }

    @Transactional
    public void deleteSpace(Long id) {
        if (!spaceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Espacio no encontrado con id: " + id);
        }
        spaceRepository.deleteById(id);
    }
}
