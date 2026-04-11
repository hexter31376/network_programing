package dev.wonyoung.adapter.in.console.week5;

import dev.wonyoung.adapter.in.swing.week5.Subject1View;
import dev.wonyoung.application.port.in.FetchUrlUseCase;
import dev.wonyoung.domain.model.enums.ContentKind;
import dev.wonyoung.domain.model.HttpResult;
import dev.wonyoung.domain.model.UrlMeta;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import javax.swing.SwingUtilities;

/**
 * Subject1 프레젠터: URL 제출 이벤트를 받아 URL 메타 정보와 본문 조회를 요청하고 View에 결과를 전달한다.
 * <p>
 * - URL 메타(프로토콜, 호스트, 포트 등) → 상단 영역 출력
 * - 텍스트 본문 → 그대로 출력
 * - 텍스트 외 본문 → 종류 레이블 + Content-Type + 바이트 크기 출력
 * </p>
 */
@Component
public class Subject1Presenter {

    private final FetchUrlUseCase fetchUrlUseCase;
    private final Subject1View subject1View;

    @Inject
    public Subject1Presenter(FetchUrlUseCase fetchUrlUseCase, Subject1View subject1View) {
        this.fetchUrlUseCase = fetchUrlUseCase;
        this.subject1View = subject1View;
        subject1View.setOnSubmit(this::onUrlSubmit);
    }

    private void onUrlSubmit(String rawUrl) {
        subject1View.showLoading();

        fetchUrlUseCase.fetch(rawUrl, new FetchUrlUseCase.FetchCallback() {
            @Override
            public void onMeta(UrlMeta meta) {
                // URL 파싱 결과를 메타 영역에 표시
                SwingUtilities.invokeLater(() -> subject1View.showMeta(formatMeta(meta)));
            }

            @Override
            public void onBody(HttpResult result) {
                // HTTP 응답 본문을 콘텐츠 종류에 따라 포매팅하여 표시
                SwingUtilities.invokeLater(() -> subject1View.showBody(formatBody(result)));
            }

            @Override
            public void onError(String message) {
                SwingUtilities.invokeLater(() -> subject1View.showBody(message));
            }
        });
    }

    /** UrlMeta 필드를 보기 좋은 레이블 형식의 문자열로 변환한다. */
    private static String formatMeta(UrlMeta urlMeta) {
        return "Protocol   : " + urlMeta.protocol()          + '\n'
             + "Host       : " + urlMeta.host()               + '\n'
             + "Port       : " + (urlMeta.port() == -1
                   ? "(default " + urlMeta.defaultPort() + ")"
                   : urlMeta.port())                          + '\n'
             + "File       : " + urlMeta.file()               + '\n'
             + "Path       : " + urlMeta.path()               + '\n'
             + "Query      : " + nullSafe(urlMeta.query())    + '\n'
             + "Reference  : " + nullSafe(urlMeta.ref())      + '\n'
             + "UserInfo   : " + nullSafe(urlMeta.userInfo()) + '\n'
             + "Authority  : " + nullSafe(urlMeta.authority())+ '\n'
             + "HashCode   : " + urlMeta.urlHashCode()        + '\n';
    }

    /**
     * HttpResult를 ContentKind에 따라 문자열로 변환한다.
     * 텍스트는 본문을 그대로, 나머지는 종류 레이블 + 메타 정보를 반환한다.
     */
    private static String formatBody(HttpResult httpResult) {
        if (httpResult.kind() == ContentKind.TEXT) {
            return httpResult.textBody();
        }
        String label = switch (httpResult.kind()) {
            case IMAGE -> "[이미지 객체] ";
            case AUDIO -> "[오디오 객체] ";
            case VIDEO -> "[비디오 객체] ";
            default -> "[지원되지 않는 객체 유형] ";
        };
        return label + httpResult.contentType() + "  (" + httpResult.byteLength() + " bytes)";
    }

    /** null이거나 빈 문자열이면 "(none)"을 반환한다. */
    private static String nullSafe(String s) {
        return (s == null || s.isEmpty()) ? "(none)" : s;
    }
}
