package org.example.vladtech.portfolio.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.portfolio.data.PortfolioRepository;
import org.example.vladtech.portfolio.mapperlayer.PortfolioMapper;
import org.example.vladtech.portfolio.presentation.PortfolioResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;

    @Override
    public List<PortfolioResponseDto> getAllPortfolioItems() {
        log.info("Fetching all portfolio items");
        List<PortfolioItem> portfolioItems = portfolioRepository.findAll();
        return portfolioItems.stream()
                .map(portfolioMapper::entityToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioResponseDto getPortfolioItemById(String portfolioId) {
        log.info("Fetching portfolio item with id: {}", portfolioId);
        PortfolioItem portfolioItem = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio item not found with id: " + portfolioId));
        return portfolioMapper.entityToResponseDto(portfolioItem);
    }
}

