import 'dart:async';
import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'core/theme.dart';
import 'core/components.dart';
import 'widget_selection_screen.dart';

class ClockCard extends StatefulWidget {
  final bool isPreview;
  const ClockCard({super.key, this.isPreview = false});

  @override
  State<ClockCard> createState() => _ClockCardState();
}

class _ClockCardState extends State<ClockCard> {
  late Timer _timer;
  late DateTime _currentTime;

  @override
  void initState() {
    super.initState();
    _currentTime = DateTime.now();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      setState(() {
        _currentTime = DateTime.now();
      });
    });
  }

  @override
  void dispose() {
    _timer.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final card = NothingCard(
      onTap: widget.isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'clock',
            title: 'Clock',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          // Dot grid background
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const NothingLabel('TIME'),
                    Flexible(
                      child: FittedBox(
                        fit: BoxFit.scaleDown,
                        alignment: Alignment.centerLeft,
                        child: Text(
                          DateFormat('HH:mm').format(_currentTime),
                          style: NothingTheme.doto(fontSize: 64),
                        ),
                      ),
                    ),
                    Text(
                      DateFormat('EEEE, MMMM d').format(_currentTime).toUpperCase(),
                      style: NothingTheme.mono(fontSize: 11, color: NothingTheme.textPrimary),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              const SizedBox(width: NothingTheme.spaceMD),
              const Icon(
                Icons.access_time,
                size: 24,
                color: NothingTheme.textSecondary,
              ),
            ],
          ),
        ],
      ),
    );

    return card;
  }
}

class CalendarCard extends StatelessWidget {
  final bool isPreview;
  const CalendarCard({super.key, this.isPreview = false});

  @override
  Widget build(BuildContext context) {
    final now = DateTime.now();
    final card = NothingCard(
      onTap: isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'calendar',
            title: 'Calendar',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          // Dot grid background
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                decoration: BoxDecoration(
                  border: Border.all(color: NothingTheme.borderVisible),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  DateFormat('d').format(now),
                  style: NothingTheme.doto(fontSize: 32),
                ),
              ),
              const SizedBox(width: NothingTheme.spaceMD),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const NothingLabel('CALENDAR'),
                    Flexible(
                      child: FittedBox(
                        fit: BoxFit.scaleDown,
                        alignment: Alignment.centerLeft,
                        child: Text(
                          DateFormat('MMMM yyyy').format(now).toUpperCase(),
                          style: NothingTheme.mono(fontSize: 14, color: NothingTheme.textPrimary),
                        ),
                      ),
                    ),
                    Text(
                      'NO UPCOMING EVENTS',
                      style: NothingTheme.mono(fontSize: 10, color: NothingTheme.textDisabled),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
    
    return card;
  }
}
class BatteryCard extends StatelessWidget {
  final bool isPreview;
  const BatteryCard({super.key, this.isPreview = false});

  @override
  Widget build(BuildContext context) {
    final card = NothingCard(
      onTap: isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'battery',
            title: 'Battery',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const NothingLabel('BATTERY'),
                    Flexible(
                      child: FittedBox(
                        fit: BoxFit.scaleDown,
                        alignment: Alignment.centerLeft,
                        child: Text(
                          '85%',
                          style: NothingTheme.doto(fontSize: 40),
                        ),
                      ),
                    ),
                    const NothingLabel('CHARGING'),
                  ],
                ),
              ),
              const Icon(Icons.battery_charging_full, size: 32, color: NothingTheme.success),
            ],
          ),
        ],
      ),
    );

    return isPreview ? card : Hero(tag: 'widget_battery', child: card);
  }
}

class StorageCard extends StatelessWidget {
  final bool isPreview;
  const StorageCard({super.key, this.isPreview = false});

  @override
  Widget build(BuildContext context) {
    final card = NothingCard(
      onTap: isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'storage',
            title: 'Storage',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const NothingLabel('STORAGE'),
                  Text('128 GB', style: NothingTheme.mono(fontSize: 10, color: NothingTheme.textPrimary)),
                ],
              ),
              const SizedBox(height: 8),
              Flexible(
                child: FittedBox(
                  fit: BoxFit.scaleDown,
                  alignment: Alignment.centerLeft,
                  child: Text(
                    '76.8 GB USED',
                    style: NothingTheme.doto(fontSize: 24),
                  ),
                ),
              ),
              const SizedBox(height: 8),
              ClipRRect(
                borderRadius: BorderRadius.circular(2),
                child: LinearProgressIndicator(
                  value: 0.6,
                  backgroundColor: NothingTheme.border,
                  valueColor: const AlwaysStoppedAnimation(NothingTheme.textPrimary),
                  minHeight: 6,
                ),
              ),
            ],
          ),
        ],
      ),
    );

    return isPreview ? card : Hero(tag: 'widget_storage', child: card);
  }
}

class SpinnerCard extends StatelessWidget {
  final bool isPreview;
  const SpinnerCard({super.key, this.isPreview = false});

  @override
  Widget build(BuildContext context) {
    final card = NothingCard(
      onTap: isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'games',
            title: 'Spinner',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              const NothingLabel('SPINNER'),
              const SizedBox(height: 8),
              Center(
                child: Container(
                  width: 64,
                  height: 64,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: NothingTheme.textPrimary, width: 2),
                  ),
                  child: Center(
                    child: SizedBox(
                      width: 48,
                      height: 48,
                      child: Stack(
                        alignment: Alignment.center,
                        children: List.generate(4, (index) {
                          return Transform.rotate(
                            angle: index * 3.14159 / 2,
                            child: Align(
                              alignment: Alignment.topCenter,
                              child: Container(
                                width: 2,
                                height: 8,
                                color: NothingTheme.textPrimary,
                              ),
                            ),
                          );
                        }),
                      ),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 6),
              Text(
                'TAP TO SPIN',
                style: NothingTheme.mono(fontSize: 9, color: NothingTheme.textDisabled),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ],
      ),
    );
    return isPreview ? card : Hero(tag: 'widget_spinner', child: card);
  }
}

class BottleSpinCard extends StatelessWidget {
  final bool isPreview;
  const BottleSpinCard({super.key, this.isPreview = false});

  @override
  Widget build(BuildContext context) {
    final card = NothingCard(
      onTap: isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'games',
            title: 'Bottle Spin',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              const NothingLabel('BOTTLE'),
              const SizedBox(height: 8),
              Center(
                child: Container(
                  width: 56,
                  height: 64,
                  decoration: BoxDecoration(
                    border: Border.all(color: NothingTheme.textPrimary, width: 1.5),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Center(
                    child: Transform.rotate(
                      angle: -0.785,
                      child: Container(
                        width: 14,
                        height: 36,
                        decoration: BoxDecoration(
                          border: Border.all(color: NothingTheme.textPrimary, width: 1.5),
                          borderRadius: BorderRadius.circular(3),
                        ),
                        child: Align(
                          alignment: Alignment.topCenter,
                          child: Container(
                            width: 14,
                            height: 10,
                            color: NothingTheme.textPrimary,
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 6),
              Text(
                'TAP TO SPIN',
                style: NothingTheme.mono(fontSize: 9, color: NothingTheme.textDisabled),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ],
      ),
    );
    return isPreview ? card : Hero(tag: 'widget_bottle_spin', child: card);
  }
}

class DiceRollCard extends StatelessWidget {
  final bool isPreview;
  const DiceRollCard({super.key, this.isPreview = false});

  @override
  Widget build(BuildContext context) {
    final card = NothingCard(
      onTap: isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'games',
            title: 'Dice Roll',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              const NothingLabel('DICE'),
              const SizedBox(height: 8),
              Center(
                child: Container(
                  width: 64,
                  height: 64,
                  decoration: BoxDecoration(
                    border: Border.all(color: NothingTheme.textPrimary, width: 1.5),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Center(
                    child: Text(
                      '5',
                      style: NothingTheme.doto(fontSize: 28, color: NothingTheme.textPrimary),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 6),
              Text(
                'TAP TO ROLL',
                style: NothingTheme.mono(fontSize: 9, color: NothingTheme.textDisabled),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ],
      ),
    );
    return isPreview ? card : Hero(tag: 'widget_dice_roll', child: card);
  }
}

class CoinFlipCard extends StatelessWidget {
  final bool isPreview;
  const CoinFlipCard({super.key, this.isPreview = false});

  @override
  Widget build(BuildContext context) {
    final card = NothingCard(
      onTap: isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'games',
            title: 'Coin Flip',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const NothingLabel('COIN'),
                  Text(
                    'FLIP',
                    style: NothingTheme.mono(fontSize: 9, color: NothingTheme.textDisabled),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Center(
                child: Container(
                  width: 32,
                  height: 32,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: NothingTheme.textPrimary, width: 2),
                  ),
                  child: Center(
                    child: Text(
                      'H',
                      style: NothingTheme.doto(fontSize: 16),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );

    return isPreview ? card : Hero(tag: 'widget_coin_flip', child: card);
  }
}

class DinoGameCard extends StatelessWidget {
  final bool isPreview;
  const DinoGameCard({super.key, this.isPreview = false});

  @override
  Widget build(BuildContext context) {
    final card = NothingCard(
      onTap: isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'games',
            title: 'Dino Game',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const NothingLabel('DINO'),
                  Text(
                    'GAME',
                    style: NothingTheme.mono(fontSize: 9, color: NothingTheme.textDisabled),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Container(
                    width: 24,
                    height: 24,
                    color: Colors.transparent,
                    child: CustomPaint(
                      painter: DinoPixelPainter(NothingTheme.textPrimary),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Container(width: 4, height: 4, color: NothingTheme.textSecondary),
                            Container(width: 8, height: 12, color: NothingTheme.textPrimary),
                            Container(width: 4, height: 4, color: NothingTheme.textSecondary),
                          ],
                        ),
                        const SizedBox(height: 2),
                        Container(height: 1, color: NothingTheme.border),
                        const SizedBox(height: 4),
                        Text(
                          'TAP TO RUN',
                          style: NothingTheme.mono(fontSize: 9, color: NothingTheme.textDisabled),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
    return isPreview ? card : Hero(tag: 'widget_dino_game', child: card);
  }
}

class CalculatorCard extends StatelessWidget {
  final bool isPreview;
  const CalculatorCard({super.key, this.isPreview = false});

  @override
  Widget build(BuildContext context) {
    final card = NothingCard(
      onTap: isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'calculator',
            title: 'Calculator',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              const NothingLabel('CALCULATOR'),
              const SizedBox(height: 8),
              Text(
                '19',
                style: NothingTheme.doto(fontSize: 40),
              ),
              const SizedBox(height: 6),
              Text(
                '12 + 7',
                style: NothingTheme.mono(fontSize: 11, color: NothingTheme.textSecondary),
              ),
            ],
          ),
        ],
      ),
    );

    return isPreview ? card : Hero(tag: 'widget_calculator', child: card);
  }
}

class MusicCard extends StatefulWidget {
  final bool isPreview;
  const MusicCard({super.key, this.isPreview = false});

  @override
  State<MusicCard> createState() => _MusicCardState();
}

class _MusicCardState extends State<MusicCard> with SingleTickerProviderStateMixin {
  bool _isPlaying = true;
  double _progress = 0.42;
  late AnimationController _vinylSpin;
  late Timer _timer;

  @override
  void initState() {
    super.initState();
    _vinylSpin = AnimationController(
      duration: const Duration(seconds: 3),
      vsync: this,
    );
    if (_isPlaying) _vinylSpin.repeat();
    _timer = Timer.periodic(const Duration(milliseconds: 600), (_) {
      if (_isPlaying && mounted) {
        setState(() {
          _progress += 0.002;
          if (_progress >= 1.0) _progress = 0;
        });
      }
    });
  }

  @override
  void dispose() {
    _timer.cancel();
    _vinylSpin.dispose();
    super.dispose();
  }

  void _togglePlay() {
    setState(() {
      _isPlaying = !_isPlaying;
      if (_isPlaying) {
        _vinylSpin.repeat();
      } else {
        _vinylSpin.stop();
      }
    });
  }

  String _formatTime(double progress) {
    final totalSeconds = (212 * progress).round();
    final minutes = totalSeconds ~/ 60;
    final seconds = totalSeconds % 60;
    return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    final card = NothingCard(
      onTap: widget.isPreview ? null : () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => const WidgetSelectionScreen(
            category: 'music',
            title: 'Music',
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: DotGridPainter(),
            ),
          ),
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Row(
                      children: [
                        const NothingLabel('NOW PLAYING'),
                        const Spacer(),
                        if (_isPlaying)
                          Container(
                            width: 5,
                            height: 5,
                            decoration: const BoxDecoration(
                              color: NothingTheme.success,
                              shape: BoxShape.circle,
                            ),
                          ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Flexible(
                      child: FittedBox(
                        fit: BoxFit.scaleDown,
                        alignment: Alignment.centerLeft,
                        child: Text(
                          'NIGHTS',
                          style: NothingTheme.grotesk(
                            fontSize: 26,
                            fontWeight: FontWeight.w500,
                            color: NothingTheme.textPrimary,
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'FRANK OCEAN',
                      style: NothingTheme.mono(fontSize: 10),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 10),
                    ClipRRect(
                      borderRadius: BorderRadius.circular(2),
                      child: LinearProgressIndicator(
                        value: _progress,
                        backgroundColor: NothingTheme.border,
                        valueColor: const AlwaysStoppedAnimation(NothingTheme.textPrimary),
                        minHeight: 3,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          _formatTime(_progress),
                          style: NothingTheme.mono(fontSize: 9, color: NothingTheme.textDisabled),
                        ),
                        Text(
                          '03:32',
                          style: NothingTheme.mono(fontSize: 9, color: NothingTheme.textDisabled),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    GestureDetector(
                      onTap: _togglePlay,
                      child: Container(
                        width: 30,
                        height: 30,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          border: Border.all(color: NothingTheme.textPrimary, width: 1.5),
                        ),
                        child: Icon(
                          _isPlaying ? Icons.pause : Icons.play_arrow,
                          size: 16,
                          color: NothingTheme.textPrimary,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              SizedBox(
                width: 76,
                height: 76,
                child: AnimatedBuilder(
                  animation: _vinylSpin,
                  builder: (context, child) {
                    return Transform.rotate(
                      angle: _vinylSpin.value * 2 * math.pi,
                      child: child,
                    );
                  },
                  child: CustomPaint(
                    painter: _VinylPainter(NothingTheme.textPrimary, NothingTheme.textSecondary),
                    size: const Size(76, 76),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );

    return widget.isPreview ? card : Hero(tag: 'widget_music', child: card);
  }
}

class _VinylPainter extends CustomPainter {
  final Color primary;
  final Color secondary;

  _VinylPainter(this.primary, this.secondary);

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2;

    final outerPaint = Paint()
      ..color = primary
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2;
    canvas.drawCircle(center, radius - 1, outerPaint);

    final groovePaint = Paint()
      ..color = primary.withValues(alpha: 0.15)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 0.5;
    for (int i = 0; i < 4; i++) {
      canvas.drawCircle(center, radius * (0.88 - i * 0.12), groovePaint);
    }

    final labelPaint = Paint()
      ..color = primary
      ..style = PaintingStyle.fill;
    canvas.drawCircle(center, radius * 0.28, labelPaint);

    final holePaint = Paint()
      ..color = const Color(0xFF000000)
      ..style = PaintingStyle.fill;
    canvas.drawCircle(center, radius * 0.08, holePaint);

    final accentPaint = Paint()
      ..color = const Color(0xFFD71921)
      ..style = PaintingStyle.fill;
    canvas.drawCircle(Offset(center.dx + radius * 0.55, center.dy - radius * 0.55), 2.5, accentPaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class DinoPixelPainter extends CustomPainter {
  final Color color;

  DinoPixelPainter(this.color);

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;
    
    // Simple pixelated dino
    // Head
    canvas.drawRect(Rect.fromLTWH(size.width * 0.4, 0, size.width * 0.6, size.height * 0.4), paint);
    // Eye
    paint.color = color.computeLuminance() > 0.5 ? Color(0xFF000000) : Color(0xFFFFFFFF);
    canvas.drawRect(Rect.fromLTWH(size.width * 0.55, size.height * 0.1, size.width * 0.1, size.height * 0.1), paint);
    paint.color = color;
    // Mouth
    canvas.drawRect(Rect.fromLTWH(size.width * 0.6, size.height * 0.25, size.width * 0.4, size.height * 0.05), paint);
    
    // Body & Tail
    canvas.drawRect(Rect.fromLTWH(size.width * 0.2, size.height * 0.35, size.width * 0.4, size.height * 0.4), paint);
    canvas.drawRect(Rect.fromLTWH(0, size.height * 0.3, size.width * 0.2, size.height * 0.1), paint);
    
    // Legs
    canvas.drawRect(Rect.fromLTWH(size.width * 0.25, size.height * 0.75, size.width * 0.1, size.height * 0.25), paint);
    canvas.drawRect(Rect.fromLTWH(size.width * 0.45, size.height * 0.75, size.width * 0.1, size.height * 0.25), paint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

