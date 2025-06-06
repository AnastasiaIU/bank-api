package nl.inholland.bank_api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.inholland.bank_api.constant.ErrorMessages;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RequestSizeFilter extends OncePerRequestFilter {
    private static final long MAX_REQUEST_SIZE = 1024 * 1024; // 1MB

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        long length = request.getContentLengthLong();
        if (length > MAX_REQUEST_SIZE) {
            response.sendError(HttpStatus.PAYLOAD_TOO_LARGE.value(), ErrorMessages.REQUEST_PAYLOAD_TOO_LARGE);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
