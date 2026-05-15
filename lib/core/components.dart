import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'theme.dart';

class NothingCard extends StatelessWidget {
  final Widget child;
  final double? width;
  final double? height;
  final EdgeInsetsGeometry? padding;
  final bool raised;
  final VoidCallback? onTap;

  const NothingCard({
    super.key,
    required this.child,
    this.width,
    this.height,
    this.padding,
    this.raised = false,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return EntranceAnimation(
      child: GestureDetector(
        onTap: () {
          if (onTap != null) {
            HapticFeedback.lightImpact();
            onTap!();
          }
        },
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          curve: Curves.easeOut,
          width: width,
          height: height,
          padding: padding ?? const EdgeInsets.all(NothingTheme.spaceMD),
          decoration: BoxDecoration(
            color: raised ? NothingTheme.surfaceRaised : NothingTheme.surface,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
              color: NothingTheme.border,
              width: 1,
            ),
          ),
          child: child,
        ),
      ),
    );
  }
}

class EntranceAnimation extends StatelessWidget {
  final Widget child;
  final Duration delay;

  const EntranceAnimation({
    super.key,
    required this.child,
    this.delay = Duration.zero,
  });

  @override
  Widget build(BuildContext context) {
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0.0, end: 1.0),
      duration: const Duration(milliseconds: 600),
      curve: const Cubic(0.2, 0.0, 0.0, 1.0), // Nothing style ease-out
      builder: (context, value, child) {
        return Opacity(
          opacity: value,
          child: Transform.translate(
            offset: Offset(0, 20 * (1 - value)),
            child: child,
          ),
        );
      },
      child: child,
    );
  }
}

class NothingLabel extends StatelessWidget {
  final String text;
  final Color? color;

  const NothingLabel(this.text, {super.key, this.color});

  @override
  Widget build(BuildContext context) {
    return Text(
      text.toUpperCase(),
      style: NothingTheme.mono(
        fontSize: 11,
        color: color ?? NothingTheme.textSecondary,
      ),
    );
  }
}

class DotGridBackground extends StatelessWidget {
  final Widget child;

  const DotGridBackground({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      painter: DotGridPainter(),
      child: child,
    );
  }
}

class DotGridPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = NothingTheme.borderVisible.withAlpha(51)
      ..strokeWidth = 1;

    const double spacing = 16.0;
    for (double x = 0; x < size.width; x += spacing) {
      for (double y = 0; y < size.height; y += spacing) {
        canvas.drawCircle(Offset(x, y), 0.5, paint);
      }
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class NothingButton extends StatelessWidget {
  final String label;
  final VoidCallback? onTap;
  final bool primary;

  const NothingButton({
    super.key,
    required this.label,
    required this.onTap,
    this.primary = true,
  });

  @override
  Widget build(BuildContext context) {
    final isDisabled = onTap == null;
    
    return GestureDetector(
      onTap: () {
        if (onTap != null) {
          HapticFeedback.mediumImpact();
          onTap!();
        }
      },
      child: Container(
        height: 56,
        padding: const EdgeInsets.symmetric(horizontal: 32),
        decoration: BoxDecoration(
          color: isDisabled
              ? NothingTheme.border
              : (primary ? NothingTheme.textDisplay : Colors.transparent),
          borderRadius: BorderRadius.circular(999),
          border: primary || isDisabled
              ? null
              : Border.all(color: NothingTheme.borderVisible),
        ),
        alignment: Alignment.center,
        child: Text(
          label.toUpperCase(),
          style: NothingTheme.mono(
            fontSize: 14,
            color: isDisabled
                ? NothingTheme.textDisabled
                : (primary ? NothingTheme.black : NothingTheme.textPrimary),
          ),
        ),
      ),
    );
  }
}

class WidgetService {
  static const _channel = MethodChannel('widget_channel');

  static Future<bool> addWidget(String type, {String? theme}) async {
    try {
      final result = await _channel.invokeMethod<bool>('addWidget', {
        'type': type,
        'theme': theme,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      debugPrint("Failed to add widget: '${e.message}'.");
      return false;
    }
  }
}
